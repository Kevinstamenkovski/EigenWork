# EigenWorks Industrial Resource Pack

This optional Minecraft Java Edition 26.2 resource pack gives every EigenWorks
block and handheld tool a distinct industrial electronics texture.

## Install

1. Keep `eigenworks-0.2.1.jar` in `.minecraft/mods/` together with Fabric API.
2. Copy `eigenworks-industrial-resource-pack-0.2.1.zip` into
   `.minecraft/resourcepacks/`.
3. In Minecraft, open **Options > Resource Packs** and move **EigenWorks
   Industrial** to the selected side.

The pack changes visuals only. The EigenWorks mod remains required for all
blocks, items, simulation, menus, and saved state.

The checked-in `assets/` directory is the editable source of the distributable
ZIP. Run `./scripts/build-resource-pack.sh` from the repository root to rebuild
the PNG textures from the source atlas and package the ZIP.
