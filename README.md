# Qconf Over Minecraft Commands (QoMC)

<center>

<img alt="preview" src="https://raw.githubusercontent.com/AmberIsFrozen/qomc/refs/heads/main/assets/thumb.png"/></br>
<i>pull the lever, qomc!</i><br/>

</center>

---

**QoMC** automatically generates server-side config commands for Minecraft mods utilizing <a href="https://github.com/sisby-folk/kaleido-config">Kaleido Config</a> (JiJ-safe implementation of Quilt Config)

### THE POISON CHOSEN ESPECIALLY FOR KALEIDO. KALEIDO'S POISON.
A [number of mods](https://modrinth.com/collection/zZVgWFum) utilize the config library **Kaleido Config**, which is convenient as it works on all loaders and minecraft versions.

Kaleido-based configurations generates `.toml` files in the game's config directory, but these can't be changed while the game is running.

[McQoy](https://modrinth.com/mod/mcqoy) solves this by auto-generating client-side configuration GUIs which allow you to edit the configs in-game.  
This is good for singleplayer and client-side mods, but unhelpful for servers. QoMC addresses this by generating server commands instead!

### ...RIGHT.

After installation, QoMC will detect mods making use of Kaleido on startup, and auto generate a command for them.

By default, the command is `/<Mod ID>_config`, e.g. `/qomc_config`.

For mods with multiple config files, QoMC will split the commands by config, e.g. `/qomc_config demo` and `/qomc_config main`.

### IN MY DEFENSE, YOUR POISONS ALL LOOK ALIKE

For devs, QoMC integration is implicit - just use [Kaleido Config](https://github.com/sisby-folk/kaleido-config) for configuration, and relevant commands will appear when QoMC is installed.<br/>
QoMC is a purely optional runtime dependency, so don't on QoMC in your buildscripts or mod metadata - only on modrinth / curseforge.

If you've implemented your own config commands, you can disable discovery of your configs using a metadata key:

In **fabric.mod.json**:
```json
  "custom": {
    "qomc:hidden": true
  }
  ```

Or in **neoforge.mods.toml**:
```toml
  [modproperties.qomc]
  hidden=true
  ```

### HOW ABOUT DESSERT?

This project is licensed under the LGPL 3.0

QoMC takes direct inspiration from [McQoy](https://github.com/sisby-folk/mcqoy), its client-side GUI counterpart!
