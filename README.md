[![Build](https://github.com/julbme/gh-action-merge-branch/actions/workflows/maven-build.yml/badge.svg)](https://github.com/julbme/gh-action-merge-branch/actions/workflows/maven-build.yml)
[![Lint Commit Messages](https://github.com/julbme/gh-action-merge-branch/actions/workflows/commitlint.yml/badge.svg)](https://github.com/julbme/gh-action-merge-branch/actions/workflows/commitlint.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=julbme_gh-action-merge-branch&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=julbme_gh-action-merge-branch)
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/julbme/gh-action-merge-branch)

# GitHub Action to merge to a branch

The GitHub Action for merging tag, branch or commit to a branch in the GitHub repository.

## Usage

### Example Workflow file

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Merge branch
        uses: julbme/gh-action-merge-branch@v1
        with:
          from: ${{ github.ref }}
          to: branch-name
          message: Merge commit message
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### Inputs

|   Name    |  Type  |   Default    |                                                                          Description                                                                           |
|-----------|--------|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `from`    | string | `github.sha` | The reference from which to merge the branch - could be a branch, a tag, a ref or a specific SHA. By default, it takes the commit that triggered the workflow. |
| `to`      | string | `Not set`    | Name of the target branch. **Required**                                                                                                                        |
| `message` | string | ` `          | The message associated to the merge. If not set, will be the default GitHub message.                                                                           |

### Outputs

| Name  |  Type  |                                     Description                                     |
|-------|--------|-------------------------------------------------------------------------------------|
| `sha` | string | The merge commit SHA, or the target branch latest commit SHA if no merge is needed. |

## Contributing

This project is totally open source and contributors are welcome.
