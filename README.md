# 👋🧩 Morphe Patches template

Template repository for Morphe Patches.

## ❓ About

This is a template to create a new Morphe Patches repository.
TODO: Update this about section with a brief introduction/summary about this repo and what it offers.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->

<!-- Do not modify this section by hand. The patch list is generated when release.yml creates a new release.
     
     If you wish for the patches list to be collapsed, then remove the word 'EXPANDED' from the comment tag above.

     If you wish to manually keep this list updated then remove the PATCHES_START and PATCHES_END 
     comment blocks entirely. -->

#### A list of your patches will be automatically shown here after your first patches release is created.

&nbsp;

## 🚀 Get started

To start using this template, follow these steps:

1. [Setup](https://github.com/MorpheApp/morphe-documentation/blob/main/docs/morphe-development/README.md) your development environment including adding a GitHub PAT as described [here](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_1_setup.md#-prepare-the-environment).
2. [Create a new repository using this template](https://github.com/new?template_name=morphe-patches-template&template_owner=MorpheApp)
3. Set up the [build.gradle.kts](patches/build.gradle.kts) file (Specifically, the 
   [group of the project](patches/build.gradle.kts#L1), and the [About](patches/build.gradle.kts#L5-L11))
4. Set up the [README.md](README.md) file[^1] (e.g, title, description, license, 
   summary of the patches that are included in the repository), the [issue templates](.github/ISSUE_TEMPLATE)[^2]
   and the [contribution guidelines](CONTRIBUTING.md)[^3].
5. Choose a name for your patches project. Keep in mind you must use a unique name that does not 
   imply or suggest authorship by the Morphe open source project. If unsure, then simply name these
   patches after yourself ("UserXYZ Morphe patches"). See the [NOTICE](NOTICE) for details. 
6. (Optional): Add `patches-bundle.png` to the project if you want a custom icon to show in
   Morphe Manager instead of your GitHub profile avatar.

🎉 You are now ready to start creating patches!

## 🧑‍💻 Usage

To develop and release Morphe Patches using this template, some things need to be considered:

- Development starts in feature branches. Once a feature branch is ready, it is squashed and merged into the `dev` branch
- The `dev` branch is merged into the `main` branch once it is ready for release
- Semantic versioning is used to version Morphe Patches.
- [Semantic commit](https://kapeli.com/cheat_sheets/Semantic_Commits.docset/Contents/Resources/Documents/index) messages are used for commits
- Commits on the `dev` branch and `main` branch are automatically released
via the [release.yml](.github/workflows/release.yml) workflow, which is also responsible for generating the changelog
and updating the version of Morphe Patches. It is triggered by pushing to the `dev` or `main` branch.
The workflow uses the `publish` task to publish the release of Morphe Patches.
- The `buildAndroid` task is used to build Morphe Patches so that it can be used on Android.


## 🤓 Tips
- See the [patcher documentation](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/1_patcher_intro.md)
  for more examples of creating patches and fingerprints.
- Do not manually edit any generated files such as: `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`.
  These files will be automatically updated in the release action.
- Do not force push any semantic release commits or you will break the release. To 'redo' the last release then:
  - Git drop the last dev/main semantic release commit you want to redo.
  - Delete the release from the release area of this repo and delete the tag   
  - Make any other changes you wish to do
  - Force push dev/main branch
  - A new replacement release will be created by `release.yml`


## 📚 Everything else

Optionally you can include a button/link in this readme that users can click to add your 
patches to Morphe (update the links below after creating your new patches repo):

<!-- The patches end tag is intentionally placed here so the first release will cleanup 
     this readme of all developer instructions above. -->
<!-- PATCHES_END -->

#### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=xyz-user/xyz-patches

Or manually add this repository url as a patch source in Morphe: https://github.com/xyz-user/xyz-patches

### 📙 Contributing

Thank you for considering contributing to UserXYZ Morphe Patches.  
You can find the contribution guidelines [here](CONTRIBUTING.md).

### 🛠️ Building

To build UserXYZ Morphe Patches,
you can follow the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation).

## 📜 License

UserXYZ Morphe Patches are licensed under the [GNU General Public License v3.0](LICENSE)
