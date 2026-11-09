# David Garcia Karoo

A [Hammerhead Karoo 3](https://www.hammerhead.io/karoo) extension that automatically scrolls through your ride data pages, so you can focus on riding.

This is a fork of [anpaiss/AutoLoopKaroo](https://github.com/anpaiss/AutoLoopKaroo) with the Switches popup (the shared KarooSwitches library) removed, so it builds without access to that private package. All credit for the app goes to the original author. If you want the original, with the Switches field and published releases, use their repository.

## Features

- **Auto-scroll** cycles through all pages in your active ride profile at a configurable interval.
- **Page skipping**: set a page duration to **0 seconds** to skip it during the loop. Skipped pages are passed with back-to-back presses, so they never show up on screen.
- **Starts automatically** when you begin recording a ride.
- **Stops automatically** when you pause or end the ride.
- **Navigation-aware**: switches to the map page when approaching a turn, then resumes scrolling after the turn. Distances are fully configurable.
- **Toggle on/off** from the app settings or with the Karoo's **bottom-left hardware button** (the one the Karoo assigns to extension bonus actions, shared with pause), or from SRAM AXS buttons and ANT+ remotes bound to bonus actions.
- **Audible feedback** (beep and on-screen alert) when auto-scroll is