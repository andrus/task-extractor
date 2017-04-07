# task-extractor

Extracts information from GitHub commits with the goal of generating activity reports (e.g. for the purpose of time 
tracking, etc.).

## TODO:

* Connector for local Git repos
* Connector for Jira / Confluence
* Report formats switch (tab-separated, comma-separated), and appropriate escaping.
* Comment analysis, tying comments to issue tracker IDs
* Support specifying report properties in YAML
* Replace tabs in text? (sometimes imports can not be loaded to Calc)
* Parallel processing of the repos. This is pure IO, and can't be easily parallelized.