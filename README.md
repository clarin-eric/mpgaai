# MPGAAI

This is a fork of the MPGAAI project (the authentication- and authorization infrastructure for the max planck society). Used to be hosted on https://pfanne.rzg.mpg.de/svn/RZG/mpgaai/ and documented on http://pfanne.rzg.mpg.de/trac/mpgaai/.

## Development

Build:

```bash
mvn clean package verify
```

Installation and Snapshot deployment:

```bash
mvn install
mvn deploy
```

Create documentation:

```
# generic maven site generation, includes reports and javadocs, in each module's `target/site/`
mvn clean site
# combine, to be found in root: `target/staging/`
mvn site:stage
# you can view it via e.g.
python -m http.server -d target/staging/
```

Create a new Release:

```bash
mvn release:prepare
mvn release:perform
```
