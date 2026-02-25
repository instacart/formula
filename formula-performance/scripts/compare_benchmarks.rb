#!/usr/bin/env ruby
# frozen_string_literal: true

require 'json'

# Represents a single benchmark result
class BenchmarkResult
  attr_reader :name, :params, :score, :error, :unit

  def initialize(name:, params:, score:, error:, unit:)
    @name = name
    @params = params
    @score = score
    @error = error
    @unit = unit
  end

  def full_name
    return name if params.empty?

    param_str = params.sort.map { |k, v| "#{k}=#{v}" }.join(', ')
    "#{name} (#{param_str})"
  end

  def key
    return name if params.empty?

    param_str = params.sort.map { |k, v| "#{k}=#{v}" }.join('_')
    "#{name}_#{param_str}"
  end

  def percent_change(baseline)
    return 0.0 if baseline.score.zero?

    ((score - baseline.score) / baseline.score) * 100.0
  end

  def significantly_different?(other, threshold: 0.10)
    # Check if error bars overlap
    self_range = [score - error, score + error]
    other_range = [other.score - other.error, other.score + other.error]

    # No overlap means statistically different
    no_overlap = self_range[1] < other_range[0] || other_range[1] < self_range[0]

    # Also check if difference exceeds threshold
    pct_change = percent_change(other).abs
    no_overlap && pct_change >= (threshold * 100)
  end

  def format_score
    "#{score.round(3)} ± #{error.round(3)} #{unit}"
  end
end

# Parses and compares JMH benchmark results
class BenchmarkComparator
  THRESHOLD = 0.10 # 10% change threshold

  def initialize(baseline_path:, current_path:)
    @baseline = parse_results(baseline_path)
    @current = parse_results(current_path)
  end

  def compare
    {
      regressions: find_regressions,
      improvements: find_improvements,
      unchanged: find_unchanged,
      new_benchmarks: find_new_benchmarks,
      removed_benchmarks: find_removed_benchmarks,
      has_regressions: !find_regressions.empty?
    }
  end

  def generate_markdown
    results = compare

    lines = ["# 📊 Benchmark Comparison Report", ""]

    # Summary
    lines << "## Summary"
    lines << ""
    lines << (results[:regressions].empty? ? "- **Regressions**: 0 ✅" : "- **Regressions**: #{results[:regressions].size} ⚠️")
    lines << (results[:improvements].empty? ? "- **Improvements**: 0" : "- **Improvements**: #{results[:improvements].size} 🎉")
    lines << "- **Unchanged**: #{results[:unchanged].size}"
    lines << "- **New benchmarks**: #{results[:new_benchmarks].size}" unless results[:new_benchmarks].empty?
    lines << "- **Removed benchmarks**: #{results[:removed_benchmarks].size}" unless results[:removed_benchmarks].empty?
    lines << ""

    # Regressions
    unless results[:regressions].empty?
      lines << "## ⚠️ Performance Regressions"
      lines << ""
      lines << "| Benchmark | Baseline | Current | Change |"
      lines << "|-----------|----------|---------|--------|"

      results[:regressions].sort_by { |_, _, pct| -pct }.each do |curr, base, pct_change|
        lines << format_table_row(curr, base, pct_change, "🔴")
      end
      lines << ""
    end

    # Improvements
    unless results[:improvements].empty?
      lines << "## 🎉 Performance Improvements"
      lines << ""
      lines << "| Benchmark | Baseline | Current | Change |"
      lines << "|-----------|----------|---------|--------|"

      results[:improvements].sort_by { |_, _, pct| pct }.each do |curr, base, pct_change|
        lines << format_table_row(curr, base, pct_change, "🟢")
      end
      lines << ""
    end

    # Unchanged (collapsed)
    unless results[:unchanged].empty?
      lines << "<details>"
      lines << "<summary>No significant changes (#{results[:unchanged].size} benchmarks)</summary>"
      lines << ""
      lines << "| Benchmark | Baseline | Current | Change |"
      lines << "|-----------|----------|---------|--------|"

      results[:unchanged].sort_by { |curr, _, _| curr.full_name }.each do |curr, base, pct_change|
        change_str = pct_change >= 0 ? "+#{pct_change.round(1)}%" : "#{pct_change.round(1)}%"
        lines << "| #{curr.full_name} | #{base.format_score} | #{curr.format_score} | #{change_str} |"
      end
      lines << ""
      lines << "</details>"
      lines << ""
    end

    # New benchmarks
    unless results[:new_benchmarks].empty?
      lines << "<details>"
      lines << "<summary>New benchmarks (#{results[:new_benchmarks].size} added)</summary>"
      lines << ""
      lines << "| Benchmark | Score |"
      lines << "|-----------|-------|"

      results[:new_benchmarks].sort_by(&:full_name).each do |bench|
        lines << "| #{bench.full_name} | #{bench.format_score} |"
      end
      lines << ""
      lines << "</details>"
      lines << ""
    end

    # Removed benchmarks
    unless results[:removed_benchmarks].empty?
      lines << "<details>"
      lines << "<summary>Removed benchmarks (#{results[:removed_benchmarks].size} removed)</summary>"
      lines << ""
      lines << "| Benchmark | Score |"
      lines << "|-----------|-------|"

      results[:removed_benchmarks].sort_by(&:full_name).each do |bench|
        lines << "| #{bench.full_name} | #{bench.format_score} |"
      end
      lines << ""
      lines << "</details>"
      lines << ""
    end

    # Footer
    lines << "---"
    lines << "*Regressions: ±#{(THRESHOLD * 100).round(0)}% with non-overlapping confidence intervals. Improvements: ±#{(THRESHOLD * 100).round(0)}% change only.*"

    lines.join("\n")
  end

  private

  def parse_results(file_path)
    data = JSON.parse(File.read(file_path))

    results = {}
    data.each do |benchmark|
      name = benchmark['benchmark']
      params = benchmark.fetch('params', {})
      primary_metric = benchmark['primaryMetric']

      result = BenchmarkResult.new(
        name: name,
        params: params,
        score: primary_metric['score'],
        error: primary_metric['scoreError'],
        unit: primary_metric['scoreUnit']
      )

      results[result.key] = result
    end

    results
  end

  def find_regressions
    results = []
    @current.each do |key, curr|
      next unless @baseline.key?(key)

      base = @baseline[key]
      pct_change = curr.percent_change(base)

      if curr.significantly_different?(base, threshold: THRESHOLD) && pct_change > 0
        results << [curr, base, pct_change]
      end
    end
    results
  end

  def find_improvements
    results = []
    @current.each do |key, curr|
      next unless @baseline.key?(key)

      base = @baseline[key]
      pct_change = curr.percent_change(base)

      # Use only the percentage threshold for improvements (no error bar overlap
      # requirement). Improvements are informational so false positives are
      # low-cost, and requiring non-overlapping confidence intervals is overly
      # conservative — it can miss real gains when baseline error bars are wide.
      if pct_change < 0 && pct_change.abs >= (THRESHOLD * 100)
        results << [curr, base, pct_change]
      end
    end
    results
  end

  def find_unchanged
    results = []
    @current.each do |key, curr|
      next unless @baseline.key?(key)

      base = @baseline[key]
      pct_change = curr.percent_change(base)

      is_regression = curr.significantly_different?(base, threshold: THRESHOLD) && pct_change > 0
      is_improvement = pct_change < 0 && pct_change.abs >= (THRESHOLD * 100)
      unless is_regression || is_improvement
        results << [curr, base, pct_change]
      end
    end
    results
  end

  def find_new_benchmarks
    @current.select { |key, _| !@baseline.key?(key) }.values
  end

  def find_removed_benchmarks
    @baseline.select { |key, _| !@current.key?(key) }.values
  end

  def format_table_row(curr, base, pct_change, emoji)
    change_str = pct_change >= 0 ? "+#{pct_change.round(1)}%" : "#{pct_change.round(1)}%"
    "| #{curr.full_name} | #{base.format_score} | #{curr.format_score} | **#{change_str}** #{emoji} |"
  end
end

# CLI usage
if __FILE__ == $PROGRAM_NAME
  if ARGV.size != 2
    puts "Usage: #{$PROGRAM_NAME} <baseline.json> <current.json>"
    exit 1
  end

  baseline_path = ARGV[0]
  current_path = ARGV[1]

  unless File.exist?(baseline_path)
    warn "Error: Baseline file not found: #{baseline_path}"
    exit 1
  end

  unless File.exist?(current_path)
    warn "Error: Current results file not found: #{current_path}"
    exit 1
  end

  begin
    comparator = BenchmarkComparator.new(baseline_path: baseline_path, current_path: current_path)
    report = comparator.generate_markdown
    results = comparator.compare

    puts report

    if results[:has_regressions]
      warn "\n⚠️ Performance regressions detected!"
      exit 1
    else
      warn "\n✅ No performance regressions detected"
      exit 0
    end
  rescue StandardError => e
    warn "Error comparing benchmarks: #{e.message}"
    warn e.backtrace.join("\n")
    exit 2
  end
end
