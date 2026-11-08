<!-- PROJECT DESCRIPTION -->
<!--suppress HtmlDeprecatedAttribute -->
<br />

<p align="Center">
  <a href="https://github.com/AlpsBTE/PlotSystem">
    <img src="https://raw.githubusercontent.com/BuildTheEarth/assets/60fa2c8b84d271e52c4d643c23d219c31642ee56/images/global_plot_system/plot-256.png" alt="Plot System Logo" />
  </a>
</p>

<h1 align="Center">Plot System</h1>

<p align="Center">
  <b>An easy to use building system for the BuildTheEarth project.</b>
  <br/>
  <a href="https://github.com/AlpsBTE/Plot-System/wiki"><strong>Explore the docs »</strong></a>
  <br/><br/>
</p>

<p align="Center">
    <a href="https://github.com/BuildTheEarth"><img src="https://img.shields.io/badge/project-community-orange.svg?colorA=3c3c3c&label=BuildTheEarth" alt="BuildTheEarth"></a>
    <a href="https://discord.com/invite/vgkspay"><img src="https://img.shields.io/discord/696795397376442440?label=discord" alt="discord"></a>
</p>
<p align="Center">
    <a href="https://github.com/AlpsBTE/Plot-System/actions/workflows/build.yml"><img src="https://github.com/AlpsBTE/Plot-System/actions/workflows/build.yml/badge.svg" alt="Java CI with Gradle"></a>
    <a href="https://github.com/AlpsBTE/Plot-System"><img src="https://img.shields.io/codacy/grade/d5fad560dea64722887044e492b05ea9" alt="code quality"></a>
    <a href="https://github.com/AlpsBTE/Plot-System/releases"><img src="https://img.shields.io/github/v/release/AlpsBTE/Plot-System" alt="releases"></a>
    <a href="https://github.com/AlpsBTE/Plot-System"><img src="https://img.shields.io/github/repo-size/AlpsBTE/Plot-System" alt="repo size"></a>
</p>  

# About The Project
The Plot System is an essential part of any BuildTheEarth server system,
meant to run alongside the classic Terra servers to make participating a lot easier
and appeal to both the casual and power users.
The Plot-System gives every player the opportunity to participate in the project with the latest Minecraft version.

After clicking on the Companion in the inventory, a menu appears in which you can select a city from the list.
If you now click on one of these cities,
you will be taken to a random plot in this city and can start building immediately.
Configurable map links (Google Maps, Google Earth, OpenStreetMap, Apple Look Around, …) appear when you enter the plot,
allowing you to quickly find your way around.

The system is designed to make it easier for inexperienced people,
casual players and applicants to start building in BTE and to massively shorten application times.

The plugin is mostly developed by the Alps-BTE team and is intended for everyone who wants to use and contribute to it.
If you have any questions regarding the setup or contribution, please do not hesitate to contact us.

# Features
## Plot Types
![FocusMode_PlotSystemBanner](https://user-images.githubusercontent.com/64250550/184678178-f8b5c3e5-ec35-4b6b-9b1b-ac3170148d34.png)<font size="3">
The focus mode is the old well-known mode that was also used by default before the release of v3.0. Build your plot on a
floating island in an empty void world. Simple and straightforward, like in the good old days.
<br/><br/>➖ No Environment
<br/>➖ No Neighbouring Plots
</font><br/><br/>![InspirationMode_PlotSystemBanner](https://user-images.githubusercontent.com/64250550/184681201-2f7e2103-df8d-42a3-93b0-ce42770e4fa8.png)<font size="3">
The inspiration mode feels like building in a normal world. By adding the surrounding area of your plot, you will see
neighbouring buildings, streets and vegetation. In comparison to the focus mode, you will have a better orientation
which helps you when building. This mode is selected as default, when using the Plot System for the first time.
<br/><br/>➕ Environment
<br/>➖ No Neighbouring Plots
</font><br/><br/>![CityInspirationMode_PlotSystemBanner](https://user-images.githubusercontent.com/64250550/184683030-27f1760b-09ad-43f7-b5ed-bdd46c972246.png)<font size="3">
The city inspiration mode will give you the full building experience by combining the inspiration mode with other
players. Build with your friends and others in one world next to each other and see continuous progress.
<br/><br/>⚠️ This mode is not fully stable and still in BETA
<br/><br/>➕ Environment
<br/>➕ Neighbouring Plots
</font>

Setup & admin notes: **[Wiki – Plot Types](https://github.com/AlpsBTE/Plot-System/wiki/Plot-Types)**

## Tutorials
The main goal of the tutorial system is to assist players who are new to the Build The Earth project. With the help of a **Get Started**
Tutorial, which needs to be completed to use the Plot-System and other features, new players will be guided through the casual Build The Earth
building process to understand how it works.
<br/><br/><img src="https://github.com/AlpsBTE/Plot-System/assets/64250550/c3973f54-0840-4ba2-bc3e-2673ae51e07c.gif" width="50%" height="50%"/>

Config & schematic paths: **[Wiki – Tutorials](https://github.com/AlpsBTE/Plot-System/wiki/Tutorials)**

## Other Features
✔️ Support for **multiple cities** as well as for **continents** and **countries/states**<br/>
✔️ **User-friendly menus** and **commands**<br/>
✔️ Inbuilt **Get Started Tutorial** for beginners<br/>
✔️ **Multi-Language** Support (Crowdin)<br/>
✔️ Plot **Group System** (Build together with up to 5 people on one plot)<br/>
✔️ **Review 2.0** — Accuracy, Block Palette, and per-team toggle criteria with feedback history<br/>
✔️ Three plot **difficulty levels** (easy, medium and hard)<br/>
✔️ Building **Quality of Life tools**<br/>
✔️ Full **/tpll support** on plots<br/>
✔️ **Leaderboards** via DecentHolograms<br/>
✔️ Automatic **abandoning of inactive plots**<br/>
✔️ Automatic **placement of completed plots** on the terra server<br/>
✔️ Standalone **[integration API](https://github.com/AlpsBTE/Plot-System/wiki/API-and-Integrations)** for reminders and third-party plugins<br/>
✔️ **Easy configuration** and **setup**<br/>

# Installation
💻 To use this system, you currently need at least two servers.</br>
One server runs [Terra+-](https://github.com/BTE-Germany/TerraPlusMinus) together with the
**[Plot-System-Terra](https://github.com/AlpsBTE/Plot-System-Terra)** plugin, while the other is a Paper server
dedicated to the Plot-System itself.</br>
**For more information check out the [Wiki](https://github.com/AlpsBTE/Plot-System/wiki/Installation)**!

### Downloads
* **Stable releases** — [GitHub Releases](https://github.com/AlpsBTE/Plot-System/releases) (recommended for production)
* **CI / staging builds** — every push to `main` (and pull requests) runs [Java CI with Gradle](https://github.com/AlpsBTE/Plot-System/actions/workflows/build.yml) and uploads a `Staging-Build` artifact. Latest `main` build (no GitHub login): [nightly.link – Staging-Build](https://nightly.link/AlpsBTE/Plot-System/workflows/build/main/Staging-Build.zip). These builds track development; prefer releases for production servers.

# Contributing
See **[Wiki – Contributing](https://github.com/AlpsBTE/Plot-System/wiki/Contributing)** (how to help, commit conventions, builds).

# License
Distributed under the MIT License. See `LICENSE` for more information.

# Libraries & APIs
See **[Wiki – Libraries & APIs](https://github.com/AlpsBTE/Plot-System/wiki/Libraries-and-APIs)**.
