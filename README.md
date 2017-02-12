# task-extractor

Extracts information from GitHub commits with the goal of generating activity reports (e.g. for the purpose of time 
tracking, etc.).

## TODO:

* By default automatically build the time range to cover the last month
* Connector for local Git repos
* Connector for Jira / Confluence
* Report formats switch (tab-separated, comma-separated), and appropriate escaping.
* Route logback to stderr
* Comment analysis, tying comments to issue tracker IDs
* Support specifying report properties in YAML
* Replace tabs in text? (sometimes imports can not be loaded to Calc)