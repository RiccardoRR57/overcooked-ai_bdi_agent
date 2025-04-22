+timestep(0) : true <- 
    .print("timestep: 0");
    !cook.

+!exec_action(A) : timestep(N) 
    <-  A;
        .wait(timestep(N+1));
        .print("executed action: ", A).

+!exec_action(A) : true 
    <-  .wait(timestep(_));
        !exec_action(A).


+width(W) : true <- .print("width: ", W).

+height(H) : true <- .print("height: ", H).

+cell(T,X,Y) : true <- .print("cell: ", T, ", ", X, ", ", Y).

+object(T,X,Y) : true <- .print("object: ", T, ", ", X, ", ", Y).

+player(X,Y,Dx,Dy,Holding) : true <- .print("me: ", X, ", ", Y, ", ", Dx, ", ", Dy, ", ", Holding).

+other_player(X,Y,Dx,Dy,Holding) : true <- .print("other: ", X, ", ", Y, ", ", Dx, ", ", Dy, ", ", Holding).

+timestep(N) : true <- .print("timestep: ", N).

+order(I1,I2,I3) : true <- .print("order: ", I1, ", ", I2, ", ", I3).

+bonus_order(I1,I2,I3) : true <- .print("bonus order: ", I1, ", ", I2, ", ", I3).

+!cook : order(I1,I2,I3) <- 
    !go_towards(I1);
    !exec_action(interact);
    !go_towards(pot);
    !exec_action(interact);
    !go_towards(I2);
    !exec_action(interact);
    !go_towards(pot);
    !exec_action(interact);
    !go_towards(I3);
    !exec_action(interact);
    !go_towards(pot);
    !exec_action(interact);
    !exec_action(interact);
    !go_towards(dish);
    !exec_action(interact);
    !go_towards(pot);
    while(player(_,_,_,_,dish)) {
        .print("tento di prendere la zuppa");
        !exec_action(interact);
    }
    !go_towards(serve);
    !exec_action(interact);
    !cook.

/* Piano principale */
+!go_towards(Object) : cell(Object, EndX, EndY) & player(StartX, StartY,_,_,_)<-
    .print("Cerco il percorso da (", StartX, ",", StartY, ") a (", EndX, ",", EndY, ")");
    !simple_navigate(EndX, EndY).

+!go_towards(Object) : player(StartX, StartY,_,_,_) <-
    .print("sono nella cella (", StartX, ",", StartY, ")");
    .print("non so dove andare per ", Object).

// Simple navigation algorithm
+!simple_navigate(GoalX, GoalY) <-
    // Get current position
    ?player(CurrentX, CurrentY,_,_,_);
    .print("[NAV] Current: (", CurrentX, ",", CurrentY, ") Target: (", GoalX, ",", GoalY, ")");
    
    // Check if we're already adjacent to the goal
    if ((CurrentX == GoalX & (CurrentY == GoalY-1 | CurrentY == GoalY+1)) | 
        (CurrentY == GoalY & (CurrentX == GoalX-1 | CurrentX == GoalX+1))) {
        .print("[NAV] Goal reached! Adjacent to destination");
        // Now face the destination
        !face_target(GoalX, GoalY);
    } else {
        // Try to move one step closer
        !move_step_towards(GoalX, GoalY);
        
        // Recursively continue navigation
        !simple_navigate(GoalX, GoalY);
    }.

// Plan to make the agent face the target
+!face_target(GoalX, GoalY) : player(CurrentX, CurrentY,Dx,Dy,_)<-
    // Calculate direction vector to target
    TargetDx = GoalX - CurrentX;
    TargetDy = GoalY - CurrentY;
    
    .print("[FACE] Current direction: (", Dx, ",", Dy, "), Target direction: (", TargetDx, ",", TargetDy, ")");
    
    // Check if we're already facing the target
    if (Dx == TargetDx & Dy == TargetDy) {
        .print("[FACE] Already facing the target");
    } else {
        // Determine which direction to turn
        if (TargetDx == 1 & TargetDy == 0) {
            // Need to face east
            .print("[FACE] Need to face EAST");
            !turn_to(east);
        } else {
            if (TargetDx == -1 & TargetDy == 0) {
                // Need to face west
                .print("[FACE] Need to face WEST");
                !turn_to(west);
            } else {
                if (TargetDx == 0 & TargetDy == 1) {
                    // Need to face south
                    .print("[FACE] Need to face SOUTH");
                    !turn_to(south);
                } else {
                    if (TargetDx == 0 & TargetDy == -1) {
                        // Need to face north
                        .print("[FACE] Need to face NORTH");
                        !turn_to(north);
                    }
                }
            }
        }
    }.

// Plan to turn to a specified direction
+!turn_to(Direction) : player(_,_, Dx, Dy,_) <-
    .print("[TURN] Turning to face ", Direction);
    
    if ((Direction == east & Dx == 1 & Dy == 0) |
        (Direction == west & Dx == -1 & Dy == 0) |
        (Direction == south & Dx == 0 & Dy == 1) |
        (Direction == north & Dx == 0 & Dy == -1)) {
        // Already facing the right direction
        .print("[TURN] Already facing ", Direction);
    } else {
        // Execute the appropriate action to turn
        !exec_action(Direction);
    }.

// Move one step towards the goal
+!move_step_towards(GoalX, GoalY) : player(CurrentX, CurrentY,_,_,_) <-
    // Determine best direction to move (X coordinate first, then Y)
    if (CurrentX < GoalX) {
        .print("[PATH] Trying to move EAST (X alignment)");
        // Try to move east
        !try_move(east, GoalX, GoalY);
    } else {
        if (CurrentX > GoalX) {
            .print("[PATH] Trying to move WEST (X alignment)");
            // Try to move west
            !try_move(west, GoalX, GoalY);
        } else {
            // X is aligned, focus on Y
            if (CurrentY < GoalY) {
                .print("[PATH] Trying to move SOUTH (Y alignment)");
                // Try to move south - Y increases going south
                !try_move(south, GoalX, GoalY);
            } else {
                if (CurrentY > GoalY) {
                    .print("[PATH] Trying to move NORTH (Y alignment)");
                    // Try to move north - Y decreases going north
                    !try_move(north, GoalX, GoalY);
                }
            }
        }
    }.

// Try to move in the given direction, check for obstacles
+!try_move(Direction, GoalX, GoalY) : player(CurrentX, CurrentY,_,_,_) <-
    // Calculate next position based on direction
    if (Direction == east) {
        NextX = CurrentX + 1;
        NextY = CurrentY;
    } else {
        if (Direction == west) {
            NextX = CurrentX - 1;
            NextY = CurrentY;
        } else {
            if (Direction == north) {
                NextX = CurrentX;
                NextY = CurrentY - 1;  // North means decreasing Y
            } else {
                NextX = CurrentX;
                NextY = CurrentY + 1;  // South means increasing Y
            }
        }
    }
    
    .print("[MOVE] Attempting to move ", Direction, " to (", NextX, ",", NextY, ")");
    
    // Check if next position has obstacle
    if (cell(CellType, NextX, NextY)) {
        .print("[OBSTACLE] Found cell type '", CellType, "' at (", NextX, ",", NextY, ")");
        // Obstacle found, try alternative direction
        !try_alternative_move(Direction, GoalX, GoalY);
    } else {
        if (object(ObjType, NextX, NextY)) {
            .print("[OBSTACLE] Found object '", ObjType, "' at (", NextX, ",", NextY, ")");
            // Obstacle found, try alternative direction
            !try_alternative_move(Direction, GoalX, GoalY);
        } else {
            // No obstacle, move in the chosen direction
            .print("[MOVE] Path clear, executing: ", Direction);
            !exec_action(Direction);
        }
    }.

// Try alternative directions when obstacle encountered
+!try_alternative_move(Direction, GoalX, GoalY) : player(CurrentX, CurrentY,_,_,_) <-
    .print("[REROUTE] Finding alternative to ", Direction, " movement");
    // If obstacle along X axis, try moving on Y axis
    if (Direction == east | Direction == west) {
        if (CurrentY > GoalY) {
            .print("[REROUTE] Trying NORTH instead");
            !try_move(north, GoalX, GoalY);
        } else {
            .print("[REROUTE] Trying SOUTH instead");
            !try_move(south, GoalX, GoalY);
        }
    } else {
        // If obstacle along Y axis, try moving on X axis
        if (CurrentX < GoalX) {
            .print("[REROUTE] Trying EAST instead");
            !try_move(east, GoalX, GoalY);
        } else {
            .print("[REROUTE] Trying WEST instead");
            !try_move(west, GoalX, GoalY);
        }
    }.
