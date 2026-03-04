### Documentation
We use [MkDocs](https://www.mkdocs.org/) to run our documentation site.

Installation
```sh
pip install mkdocs==1.6.1 --index-url https://pypi.org/simple/
pip install mkdocs --index-url https://pypi.org/simple/
pip install mkdocs-material --index-url https://pypi.org/simple/
```

To test documentation changes locally
```
mkdocs serve
```

To deploy documentation changes
```
mkdocs gh-deploy
```