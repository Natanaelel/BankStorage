client only need CachedBankStorage when:
- previewing build mode
- random build mode
- placing blocks
  - already in build mode

when to update settings on client from server
- idk

validate received cache on client
make sure client options make sense
otherwise: update options clientside and send updatepacket

when to check for updates from client:
  - select bank
    - when buildmode
      - when client switches hotbar slot
      - when bank gets put into or out of selected slot
      - picked up item
      - every 20 ticks
      - when closing bank screen
      - when switched to buildmode
      - when placing item
      - when scrolling

when to send update from server:
- when in screen TODO: only if buildmode
  - updateSlot
  - on slot click
- on item pickup into bank
- when requested from client