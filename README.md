# TaskChain

## What is TaskChain?
TaskChain is a Java Control Flow framework designed for Game Developers. 

TaskChain helps facilitate running tasks on an application's "Main Thread", and parallel tasks off the main (async to main).

You define a series of tasks (Task Pipeline) in the order that they should execute, and each task defines whether it should run on the main thread or not.

TaskChain then executes your task pipe line, switching thread context where needed, passing the result from the previous task to the next.

TaskChain requires Java 8+

## I have Java 7, why does it need Java 8?
Get off your dinosaur and get on this rocket ship.

## Supported Game Platforms
  - Bukkit based Minecraft Servers (Bukkit/Spigot/Paper)
  - Sponge Minecraft Servers


## License

TaskChain (c) Daniel Ennis (Aikar) 2016.

TaskChain is licensed MIT. See [LICENSE](LICENSE)
