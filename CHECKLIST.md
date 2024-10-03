# Checklist

It's good to have a checklist, to avoid forgetting something.

## Bump version
- read the change log of minecraft version
- update the version of fabric loader
- update the version of mappings
- update the version of dependent mods
- test the jar file in a real environment.

## Test a new version
- test interesting commands. (from a to z)
- test interesting mixins.
- test the integration with carpet-fabric.
- test the integration with luckperms.

## Some interesting tests
- Summon a fake player using `/player 1 spawn` and throw exp bottle to it. 
  - Test the luckperms integration with fake-player user.
- Issue the command `/tppos --z 64 --x 32 --y 128`.
  - Test the context passing after command redirection.
- Issue the command `/send-title Steve --mainTitle "<rainbow>Hello" --subTitle "<blue>World"
--fadeInTicks 60 --stayTicks 60 --fadeOutTicks 60`
- See if a pickaxe gets the max power level in `/emchantment`.
  - Test the lambda for power of providers.
- See an inventory display contains a shulker box.
  - Test the deep-level gui.
  - Test the shulker entity reader. 
- Try to move an afk player.
- Buy a new head in `/head`.
- Get a kit from `/kit`.
  - Test the screen gui.
- Start a sample for a production work.
  - Test the sign input gui.
  - Test the chunks iterator.
  - Test the hopper mixins.
- Teleport to an offline player's location using `/tppos offline`.
  - Test the player instance making.
  - Test the offline data reader.
- Enter afk using `/afk` and watch the player list.
- Test command requirements:
  - Test the requirement of `/warp`
  - Test the requirement of `/world`
- Test the command tree for a default user.
- Test platform environments:
  - The fabric server-side environment.
  - The fabric client-side environment.
  - The neoforge client-side environment.
  - The neoforge server-side environment.
- Test `/save-all`
- Test `/reload`, `/fuji reload`, `/fuji inspect fuji-commands` and `/command-bundle list`
- Special states of a player
  - as a fake-player
  - died
  - offline 
  - in the end portal
 
## Publish a new version
- sync the language files.
- update the version in "gradle.properties". (Maybe respect the `semvar` spec.)
- update change log in "CHANGELOG.md"
- test the jar file in a real environment.
- publish the pdf file in "dev" branch.
- merge changes from "dev" branch into "release" branch.

