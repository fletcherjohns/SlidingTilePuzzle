This will be a simple sliding tile puzzle application with search algorithm to solve itself.
It will have controls for the number of tiles in either direction (3x3, 3x4, 4x4, etc)

Current state:
Explained in initial commit message.

Goal:
Create PuzzleBoard View to place directly in layout given exact dimensions or fill_parent. PuzzleBoard will accept an
int[] goalState and int columnCount and will initialise a TileArea View with dimensions appropriate for the tile arrangement
and its own width and height. This TileArea should be centred within the PuzzleBoard.
Create Tile View to display each tile on the board. Tile should keep track of its own position as it is moved around. For 
drawing, it provides its actual location. For calculating puzzle state and for restricting movement of other tiles, it provides
the closest position. This simplification isn't a problem unless multitouch is used and we can avoid that.

I have another project called DailyDuckFace. It was for an assignment and I got carried away with the AdapterView to display
the photos. It worked well, so I'll share that as well so we can copy appropriate parts of the code.
