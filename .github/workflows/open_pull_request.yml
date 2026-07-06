name: Open a PR to main

on:
  push:
    branches:
      - dev
  workflow_dispatch:

permissions:
  pull-requests: write
  contents: read

jobs:
  pull-request:
    name: Open pull request
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v7

      - name: Open pull request
        env:
          GH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SOURCE_BRANCH: ${{ github.ref_name }}
          TARGET_BRANCH: main
        run: |
          # Check if PR already exists (base=main, head=dev)
          if gh pr list --base "$TARGET_BRANCH" --head "$SOURCE_BRANCH" --json number | jq -e '. | length > 0' >/dev/null; then
            echo "Pull request already exists."
            exit 0
          fi

          # Check if there are any commits between branches
          COMMITS=$(gh api "repos/${{ github.repository }}/compare/${TARGET_BRANCH}...${SOURCE_BRANCH}" --jq '.total_commits')
          if [ "$COMMITS" -eq 0 ]; then
            echo "No commits between $TARGET_BRANCH and $SOURCE_BRANCH. Skipping PR creation."
            exit 0
          fi

          # Try to create the PR
          gh pr create \
            --base "$TARGET_BRANCH" \
            --head "$SOURCE_BRANCH" \
            --title 'chore: Merge branch `dev` to `main`' \
            --body "When dev (pre-release) is ready, then merge this pull request to create a new stable release." \
            || {
              echo "::error::Pull request creation failed. Ensure 'Allow GitHub Actions to create and approve pull requests' is enabled in the repo Settings > Actions > General > Workflow permissions"
              exit 1
            }
