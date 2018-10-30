# TaskChain - Current Release Version: <!--VERSION-->3.7.2<!--VERSION-->
## What is TaskChain?
TaskChain is a Java Control Flow framework designed for Game Developers. 

TaskChain helps facilitate running tasks on an application's "Main Thread", and parallel tasks off the main (async to main).

You define a series of tasks (a Task Pipeline) in the order that they should execute, and the registration of each task defines whether it should run on the main thread or not.

TaskChain then executes your task pipe line, switching thread context where needed, passing the result from the previous task to the next.

## Documentation / Getting Started
1. [Why you should use TaskChain](https://github.com/aikar/TaskChain/wiki/why-taskchain)
2. [Learning TaskChain Terminology](https://github.com/aikar/TaskChain/wiki/taskchain-terminology)
3. [Using TaskChains & Examples](https://github.com/aikar/TaskChain/wiki/usage)
4. [Shared Task Chains](https://github.com/aikar/TaskChain/wiki/Shared-Task-Chains)

## Supported Game Platforms
### Currently Supported 
  - Bukkit based Minecraft Servers (Bukkit/Spigot/Paper)
    - [[GUIDE: Using in your Bukkit Plugins](https://github.com/aikar/TaskChain/wiki/implementing-bukkit)]
  - Sponge Minecraft Servers
    - [[GUIDE: Using in your Sponge Plugins](https://github.com/aikar/TaskChain/wiki/implementing-sponge)]

### Planned
  - Forge Minecraft Servers [[#2](https://github.com/aikar/TaskChain/issues/2)]

### 3rd Party
  - Nukkit Minecraft Servers [[#13](https://github.com/aikar/TaskChain/issues/13)]


### Want to add your own game?
If you wish to add support for your own game, see [Implementing a new game](https://github.com/aikar/TaskChain/wiki/implementing-a-new-game)

## JavaDocs
JavaDocs can be found at [http://taskchain.aikar.co](http://taskchain.aikar.co)

## Changelog
Please see [CHANGELOG](CHANGELOG.md)

## Why does it require Java 8+?
Get off your dinosaur and get on this rocket ship!

On a serious note, Lambdas provided a much cleaner API and easier implementations.

Dinosaurs have been dead for a long time, so get off it before you start to smell.

[Download Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

## Using Kotlin 1.1 for Bukkit Plugins?
If you are a developing Bukkit plugins in Kotlin, we actually do not recommend using TaskChain. [@okkero](https://github.com/okkero/) has
created a vastly better experience using Coroutines in Kotlin.

We recommend using his framework, [Skedule](https://github.com/okkero/Skedule/), which requires Kotlin 1.1.

Coroutines are a beautiful way to create this kind of behavior, but sadly we are not able to do that kind of stuff in Java Syntax.

Note that Skedule is only for Bukkit plugins. If you are working in a different platform, TaskChain does still work for Kotlin. 

  - Go to [Skedule](https://github.com/okkero/Skedule/)

## Older Versions
 * [v2](https://gist.github.com/aikar/77f8caee3c153074c99b): Updated to a Java 8 Lambda API. Massive redesign and new features. Very close to v3 but uses a static registration and global state, so it can only be bound to a single Bukkit Plugin    
 (note: some bugs were fixed in v3 with Shared chains done and error handlers)
 
 * [v1](https://gist.github.com/aikar/9010136): The original (ugly) API for Bukkit plugins, pre Java 8. Only version usable below java 8, but probably some bugs.


## License
TaskChain (c) Daniel Ennis (Aikar) 2014-2017.

TaskChain is licensed [MIT](https://tldrlegal.com/license/mit-license). See [LICENSE](LICENSE)
