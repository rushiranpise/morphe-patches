version: 2
updates:
  - package-ecosystem: github-actions
    labels: []
    directory: /
    target-branch: dev
    schedule:
      interval: monthly

  - package-ecosystem: npm
    labels: []
    directory: /
    target-branch: dev
    schedule:
      interval: monthly
    ignore:
      # version 10.x and later may not generate release notes correctly
      # https://github.com/conventional-changelog/conventional-changelog/issues/1495
      - dependency-name: "conventional-changelog-conventionalcommits"

  - package-ecosystem: gradle
    labels: []
    directory: /
    target-branch: dev
    schedule:
      interval: monthly
