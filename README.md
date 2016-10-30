# TaskChain - Current Release Version: <!--VERSION-->3.2.0<!--VERSION-->
## What is TaskChain?
TaskChain is a Java Control Flow framework designed for Game Developers. 

TaskChain helps facilitate running tasks on an application's "Main Thread", and parallel tasks off the main (async to main).

You define a series of tasks (a Task Pipeline) in the order that they should execute, and the registration of each task defines whether it should run on the main thread or not.

TaskChain then executes your task pipe line, switching thread context where needed, passing the result from the previous task to the next.

## Documentation / Getting Started
1. [Why you should use TaskChain](https://github.com/aikar/TaskChain/wiki/why-taskchain)
2. [Learning TaskChain Terminology](https://github.com/aikar/TaskChain/wiki/taskchain-terminology)
3. [Using TaskChains & Examples](https://github.com/aikar/TaskChain/wiki/usage)

## Supported Game Platforms
### Currently Supported
  - Bukkit based Minecraft Servers (Bukkit/Spigot/Paper)  
  [[GUIDE: Using in your Plugins](https://github.com/aikar/TaskChain/wiki/implementing-bukkit)]
  

### In Progress:
  - Sponge Minecraft Servers [[#1](https://github.com/aikar/TaskChain/issues/1)]

### Planned
  - Forge Minecraft Servers [[#2](https://github.com/aikar/TaskChain/issues/2)]

### Want to add your own game?
If you wish to add support for your own game, see [Implementing a new game](https://github.com/aikar/TaskChain/wiki/implementing-a-new-game)

## Changelog
Please see [CHANGELOG](CHANGELOG.md)

## Why does it require Java 8+?
Get off your dinosaur and get on this rocket ship!

On a serious note, Lambdas provided a much cleaner API and easier implementations.

Dinosaurs have been dead for a long time, so get off it before you start to smell.

[Download Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

## Older Versions
 * [v2](https://gist.github.com/aikar/77f8caee3c153074c99b): Updated to a Java 8 Lambda API. Massive redesign and new features. Very close to v3 but uses a static registration and global state, so it can only be bound to a single Bukkit Plugin    
 (note: some bugs were fixed in v3 with Shared chains done and error handlers)
 
 * [v1](https://gist.github.com/aikar/9010136): The original (ugly) API for Bukkit plugins, pre Java 8. Only version usable below java 8, but probably some bugs.


## License
TaskChain (c) Daniel Ennis (Aikar) 2016.

TaskChain is licensed [MIT](https://tldrlegal.com/license/mit-license). See [LICENSE](LICENSE)
