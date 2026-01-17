# QoMC
Formally **Quilt Config over Minecraft Command**, this is a mod which automagically generates a config commands for mods targeting [Kaleido Config](https://github.com/sisby-folk/kaleido-config) (JiJ-safe implementation of Quilt Config).

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