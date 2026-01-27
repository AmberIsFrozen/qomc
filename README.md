# Qconf Over Minecraft Commands (QoMC)

<center>

<img alt="preview" src="https://raw.githubusercontent.com/AmberIsFrozen/qomc/refs/heads/main/assets/thumb.png"/></br>
<i>pull the lever, qomc!</i><br/>
Auto-generated server-side config commands for Minecraft mods utilizing <a href="https://github.com/sisby-folk/kaleido-config">Kaleido Config</a> (JiJ-safe implementation of Quilt Config)!

</center>

---

## Some background
A [number of mods](https://modrinth.com/collection/zZVgWFum) utilize a config library called **Kaleido Config**, which relieves the work from Mod Developers to implement config read/saving.

By default they generate a `.toml` file in the game's config directory that user can edit. However often-times they aren't quite editable in-game.

[McQoy](https://modrinth.com/mod/mcqoy) solves this by auto-generating a GUI config screen which you can edit in-game.  
However this isn't suitable for dedicated minecraft server which doesn't have a GUI. This mod solves this by auto-generating a Minecraft command for config.

## Usage
<details>
    <summary>For players</summary>

After installation, QoMC will detect mods making use of Kaleido on startup, and auto generate a command for them.

By default, the command name would be `/<Mod ID>_config`, for example `/qomc_config`.

For mods with multiple config, QoMC also allows you to specify different config, e.g. `/qomc_config demo` and `/qomc_config main`.

</details>
<details>
    <summary>For developers</summary>

This is a mod which automatically generates a config command for players. Developers should not need to concern about depending on this mod.  
All they need to do is to use [Kaleido Config](https://github.com/sisby-folk/kaleido-config) to handle their configs, and QoMC will automatically discover the config and generate the corresponding command.
    
### Disable auto-command generation
If your mod already supply a first-party config command, it may be desirable to disable the auto-generated command from QoMC. This can be done by adding the corresponding field in the mod metadata.

**Fabric Mod**
- Add the following to your **fabric.mod.json**:
- ```json
  "custom": {
    "qomc:hidden": true
  }
  ```

**NeoForge Mod**
- Add the following to your **neoforge.mods.toml**:
- ```toml
  [modproperties.qomc]
  hidden=true
  ```

</details>

## Credits & Licenses
This project is licensed under the LGPL 3.0.

Many codes & ideas are inspired by [McQoy](https://github.com/sisby-folk/mcqoy), the GUI counterpart of QoMC!
