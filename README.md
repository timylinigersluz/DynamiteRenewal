# DynamiteRenewal

A Minecraft plugin for dynamic minigame worlds that automatically reset themselves after a configurable time — but **only after a player has joined the world**. Built for use with [Multiverse-Core](https://dev.bukkit.org/projects/multiverse-core) and designed for PaperMC 1.21.4.

---

## 🌍 What It Does

- Supports **auto-resetting worlds** for minigames, adventure maps or events.
- World resets are **triggered only after a player enters** the target world.
- Configurable warnings before resets.
- Full **inventory handling** and teleportation using fallback or original world.
- Integration with Multiverse-Core to manage worlds cleanly.
- Optional support for **custom resource packs** and **starting inventories**.
- Designed with stability and low server impact in mind.

---

## 🧱 Requirements

- **PaperMC 1.20 – 1.21.4**
- **Multiverse-Core** installed and loaded
- Java 17+

---

## ⚙️ Installation

1. Download the plugin `.jar` file and place it in your server's `/plugins` folder.
2. Make sure [Multiverse-Core](https://dev.bukkit.org/projects/multiverse-core) is also installed.
3. Restart the server.
4. A default `config.yml` will be generated.

---

## 🧾 Commands

| Command                      | Description                                      | Permission         |
|-----------------------------|--------------------------------------------------|--------------------|
| `/dynamite create <name>`   | Create a new world                               | `dynamite.admin`   |
| `/dynamite delete <name>`   | Delete a world and remove from config            | `dynamite.admin`   |
| `/dynamite start <name>`    | Schedule world reset once a player joins         | `dynamite.mod`     |
| `/dynamite stop <name>`     | Stop the current reset task                      | `dynamite.mod`     |
| `/dynamite backup <name>`   | Backup the world                                 | `dynamite.admin`   |
| `/dynamite restore <name>`  | Manually restore the world                       | `dynamite.admin`   |
| `/dynamite setspawn`        | Set the current player position as world spawn   | `dynamite.mod`     |
| `/dynamite exit`            | Teleport player back to origin or fallback world | everyone           |
| `/dynamite reload`          | Reload configuration                             | `dynamite.admin`   |

---

## 🔧 Configuration (`config.yml`)

```yaml
defaultGameMode: "CREATIVE"
exceptionWorlds:
  - world

resetTime: 60
firstWarningTime: 30
secondWarningTime: 10
fallbackServer: "world"

log: true
loggerPrefix: "[Dynamite-Debug]"

resourcePackUrl: "https://example.com/respack.zip"
resourcePackHash: "yourhashhere"
alternateResourcePackUrl: ""
alternateResourcePackHash: ""

dynamiteWorlds:
  - lobby

startingItems:
  netherite_sword:
    material: NETHERITE_SWORD
    amount: 1
  netherite_armor:
    - material: NETHERITE_HELMET
      amount: 1
      enchantments:
        - enchantment: "PROTECTION_ENVIRONMENTAL"
          level: 4
```

You can define full inventories, enchantments, resource packs, and default gamemodes per world.

---

## 🧠 How It Works

- The world reset process is **inactive until the first player joins** the target world.
- Once a player enters, a repeating task begins counting down.
- Players are warned via chat at defined intervals (e.g., 30s and 10s before reset).
- At 0 seconds, the world is unloaded and replaced with the latest backup.
- Inventories are cleared/saved/loaded depending on transition logic.

---

## 👥 Permissions

```yaml
dynamite.admin:
  description: Full admin access to create/delete/restore/reset worlds.
  default: op

dynamite.mod:
  description: Access to start/stop timers, set spawn.
  default: op
```

---

## 📦 Development & Contribution

Built with Maven using:

```xml
<dependency>
    <groupId>io.papermc.paper</groupId>
    <artifactId>paper-api</artifactId>
    <version>1.21.4-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

---

## 📣 Credits

Plugin by [ksrminecraft.ch](https://ksrminecraft.ch)  
World management powered by [Multiverse-Core](https://github.com/Multiverse/Multiverse-Core)

---

## 📜 License

MIT License – feel free to use, adapt, and contribute!
