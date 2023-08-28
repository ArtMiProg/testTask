# testTask
testTask is a Java application that interacts with the GitHub API to retrieve and filter repository information.

## Features

- Retrieve user repositories from GitHub API
- Filter repositories which are not forks
- Handle exceptions and error responses gracefully

## Table of Contents

[Usage](#usage)

#Usage
For using the testTask application successfully, create application.properties file with the next information:

server.port={Your port}
logging.level.root=INFO
github.api.base.url=https://api.github.com
github.api.pat={YOUR GITHUB PERSONAL ACCESS TOKEN}

If you don't have a PAT, create it and past to application.properties file.
If you are not able to create a PAT or ypu don't wanna do it, delete the next row from GitHubRepController: "headers.set("Authorization", "Bearer " + PAT);"
Pay attention that without PAT you ability to use application, it means to retrieve user repositories from GitHub API will be limited according to GitHub documentation
