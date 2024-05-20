[![Check and Lint Code](https://github.com/nplay-team/moderation-bot/actions/workflows/lint.yml/badge.svg)](https://github.com/nplay-team/moderation-bot/actions/workflows/lint.yml)
[![Deploy](https://github.com/nplay-team/moderation-bot/actions/workflows/deploy.yml/badge.svg)](https://github.com/nplay-team/moderation-bot/actions/workflows/deploy.yml)
![Generic badge](https://img.shields.io/badge/Version-1.0.0-86c240".svg)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
<a href="https://discord.gg/qcpeZQhJf5">
<img src="https://discordapp.com/api/guilds/367353132772098048/embed.png" alt="discord">
</a>

<img align="right" src="https://cdn.discordapp.com/attachments/545967082253189121/1242175150636531873/NPLAY.png?ex=664ce142&is=664b8fc2&hm=1297d2602ba876e5892892afc9a45d019a2c420f328f289e712a59b0ab4af05a&" height=200 width=200>

# NPLAY-Bot

This bot was created specifically for the Discord [server](https://discord.gg/qcpeZQhJf5) of the german YouTuber and Twitch Streamer [NPLAY](https://www.youtube.com/user/nordrheintvplay). The sole purpose of the bot is to provide custom moderation features.

## Test Server

The bot is in constant development. Join the test [server](https://discord.gg/JYWezvQ) to receive regular updates, make suggestions and test preview versions. This is also the place to get support if you want to host the bot by yourself.

## Installation

Due to the high level of customization, we do not provide a public instance that anyone can invite. However, you can still host your own version of the bot. Therefore, you should have a basic understanding of the Node ecosystem, MySQL, Discord bots in general and Docker.

First clone the repository:

```
git clone https://github.com/nplay-team/moderation-bot.git
```

After you've cloned the repository, make sure to have an `.env` file providing the required variables. You can find an example [here](https://github.com/nplay-team/moderation-bot/blob/main/.env.example).

Then you can start the bot by running:

```
docker compose up
```

The MySQL server runs on port `3306`. You might want to adjust some values in order to meet your criterias.
