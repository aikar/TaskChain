# TaskChain
## What is TaskChain?
TaskChain is a Java Control Flow framework designed for Game Developers. 

TaskChain helps facilitate running tasks on an application's "Main Thread", and parallel tasks off the main (async to main).

You define a series of tasks (a Task Pipeline) in the order that they should execute, and the registration of each task defines whether it should run on the main thread or not.

TaskChain then executes your task pipe line, switching thread context where needed, passing the result from the previous task to the next.


## Documentation / Getting Started
Please see the [Wiki](wiki) for getting started with TaskChain.

## Why does it require Java 8+?
Get off your dinosaur and get on this rocket ship!

On a serious note, Lambdas provided a much cleaner API and easier implementations.

Dinosaurs have been dead for a long time, so get off it before you start to smell.

## Supported Game Platforms
### Currently Supported
  - Bukkit based Minecraft Servers (Bukkit/Spigot/Paper)
### In Progress:
  - Sponge Minecraft Servers
### Planned
  - Forge
### Want to add your own game?
If you wish to add support for your own game, see [Implementing a Game](wiki/implementing-a-new-game)
  
## Changelog
Please see [CHANGELOG](CHANGELOG.md)

## Older Versions
v2 which was based on static state can be found here: [https://gist.github.com/aikar/77f8caee3c153074c99b]
> (note: some bugs were fixed in v3 with Shared chains done and error handlers)
 
v1 which was pre Java 8 API, and completely different API design can be found here: [https://gist.github.com/aikar/9010136]


## License
TaskChain (c) Daniel Ennis (Aikar) 2016.

TaskChain is licensed [MIT](https://tldrlegal.com/license/mit-license). See [LICENSE](LICENSE)
