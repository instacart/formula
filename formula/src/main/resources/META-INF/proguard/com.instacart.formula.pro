# Formula R8/ProGuard Rules
# These rules are automatically applied to projects that depend on formula

# Prevent R8 from merging formula classes
# Formula's keying mechanism relies on class types being distinct
-keep,allowshrinking,allowobfuscation class * extends com.instacart.formula.Action
-keep,allowshrinking,allowobfuscation class * extends com.instacart.formula.IFormula
-keep,allowshrinking,allowobfuscation class * extends com.instacart.formula.Transition

