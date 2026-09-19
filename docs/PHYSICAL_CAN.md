# Physical CAN networks

Milestone 14 turns the pure CAN engine into a player-built network. Place two **Configurable CAN Node** blocks and join them with one or more **CAN Cable** blocks. Cables are thin six-way conductors and may branch in all block directions.

Use a CAN Node with an empty hand to transmit a one-byte frame. The byte increments after every successful transmission. Sneak-use selects `0x100`, `0x200`, `0x300`, or `0x7FF`. Lower identifiers retain priority when multiple frames become ready together. Use the Engineering Inspector to see connection state, configured identifier, frame counts, and the last receive diagnostic.

## Topology and timing

Every loaded cable/node block entity registers with the server-owned `CanWorldNetwork`, capped at 16,384 loaded elements per server. Registration changes mark topology dirty. On the next query or simulation advance, the network performs one bounded graph rebuild over registered elements and caches connected components. Normal transmission and bus advancement use only this cache: there is no world scan per tick.

Each connected component owns a 500 kbit/s `CanBus`. Frames retain the existing 11-bit identifier, eight-byte maximum payload, nondestructive arbitration, loser retry, bounded receive queues, and bitrate-derived duration. The server advances physical CAN in one-millisecond engineering quanta during each Minecraft server tick. Nodes in unloaded chunks and cable segments that unload are unregistered; the resulting topology is rebuilt before further delivery.

## Persistence and authority

Node identifier selection, next transmitted value, transmitted/received counts, last received identifier/value, and diagnostic survive world save/reload. Cable connectivity is derived from loaded registered blocks. The server constructs and delivers every frame; clients only request an interaction and never supply a bus result.

## Current limits

Cable models use a compact six-way cross instead of dynamically hiding unused arms. Nodes currently send an interactive diagnostic byte rather than exposing CAN registers to Eigen-8 memory-mapped I/O. Bit-level electrical dominant/recessive voltage and termination resistance are not simulated; arbitration and line time are logical protocol models.
