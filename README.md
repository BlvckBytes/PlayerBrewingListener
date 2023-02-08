# PlayerBrewingListener

A small state tracker for brewing stand blocks to check which player initialized brewing for a certain slot.

## How To Use

Instantiate the `PlayerBrewItemListener` and register it.

```java
@Override
public void onEnable() {
  getServer().getPluginManager().registerEvents(new PlayerBrewItemListener(), this);
}
```

From now on, you can listen for the custom event:

```java
@EventHandler
public void onPlayerBrew(PlayerBrewEvent e) {
  Player player = e.getPlayer();
  BrewEvent originalEvent = e.getBrewEvent();
  System.out.println(player.getName() + " brew " + e.getItem());
}
```