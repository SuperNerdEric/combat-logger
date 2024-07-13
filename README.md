# Combat Logger
Logs combat events to a text file. Upload and analyze your logs at [runelogs.com](https://runelogs.com/).  
Logs are stored in `.runelite\combat_log`

![img.png](img.png)


## Chat Command

- `::newlog` - Creates a new log file

## Reading the logs

1. **Game Ticks & Timestamps:**
    - All logs start with the current game tick count, where each game tick represents 0.6 seconds, followed by a timestamp  
    - Timestamps follow this format: `MM-DD-YYYY HH:MM:SS`  
      Example: `0 03-09-2024 15:29:27	Logged in player is Cow31337Killer`

2. **Hitsplats:**
    - Hitsplats are applied in this format: `SOURCE HITSPLAT_NAME TARGET AMOUNT`  
    - Note: The source of the hitsplat is not available unless it's your damage or a party member's damage  
    - Monsters use this format: `<NPC_id>-<unique_identifier>`  
    - The NPC id can be checked using the [Wiki Minimal OSRS NPC DB](https://chisel.weirdgloop.org/moid/npc_id.html)  
      Example: `Cow31337Killer DAMAGE_MAX_ME 12214-6613 69` - I did a max hit of 69 to The Leviathan (12214)  
      Example: `Unknown DAMAGE_ME Cow31337Killer 25` - The logged in player (Cow31337Killer) took 25 damage from an unknown source  
      Example: `Unknown DAMAGE_OTHER 7221-56938 10` - Someone else did 10 damage to Scurrius (7221)

3. **Equipment:**
    - Equipped items use their item id and can be checked using the [Wiki Minimal OSRS Item DB](https://chisel.weirdgloop.org/moid/item_id.html). You can paste the whole item id list in at once.  
      Example: `Player equipment is [19649, 21780, 12002, 27275, 21021, 21024, 19544, 13235, 28313]`

4. **Boosted Levels:**
    - Boosted levels are reported in this order: attack, strength, defence, ranged, magic, hitpoints, prayer.  
      Example: `Boosted levels are [99, 99, 99, 99, 99, 99, 99]`