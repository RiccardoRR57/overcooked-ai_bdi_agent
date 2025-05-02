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
    .print("Finding path to be adjacent to (", EndX, ",", EndY, ")");
    !find_adjacent_position(EndX, EndY, AdjX, AdjY);
    !navigate(AdjX, AdjY);
    !face_object(EndX, EndY).

+!go_towards(Object) : object(Object, EndX, EndY) & player(StartX, StartY,_,_,_)<-
    .print("Finding path to be adjacent to object (", EndX, ",", EndY, ")");
    !find_adjacent_position(EndX, EndY, AdjX, AdjY);
    !navigate(AdjX, AdjY);
    !face_object(EndX, EndY).

+!go_towards(Object) : player(StartX, StartY,_,_,_) <-
    .print("Cannot find location for ", Object).

/* Find an adjacent position to the target */
+!find_adjacent_position(TargetX, TargetY, AdjX, AdjY) : width(W) & height(H) <-
    // Try east
    AdjX1 = TargetX + 1;
    AdjY1 = TargetY;
    if (AdjX1 >= 0 & AdjX1 < W & AdjY1 >= 0 & AdjY1 < H & not cell(_, AdjX1, AdjY1) & not object(_, AdjX1, AdjY1)) {
        AdjX = AdjX1;
        AdjY = AdjY1;
    } else {
        // Try west
        AdjX2 = TargetX - 1;
        AdjY2 = TargetY;
        if (AdjX2 >= 0 & AdjX2 < W & AdjY2 >= 0 & AdjY2 < H & not cell(_, AdjX2, AdjY2) & not object(_, AdjX2, AdjY2)) {
            AdjX = AdjX2;
            AdjY = AdjY2;
        } else {
            // Try south
            AdjX3 = TargetX;
            AdjY3 = TargetY + 1;
            if (AdjX3 >= 0 & AdjX3 < W & AdjY3 >= 0 & AdjY3 < H & not cell(_, AdjX3, AdjY3) & not object(_, AdjX3, AdjY3)) {
                AdjX = AdjX3;
                AdjY = AdjY3;
            } else {
                // Try north
                AdjX4 = TargetX;
                AdjY4 = TargetY - 1;
                if (AdjX4 >= 0 & AdjX4 < W & AdjY4 >= 0 & AdjY4 < H & not cell(_, AdjX4, AdjY4) & not object(_, AdjX4, AdjY4)) {
                    AdjX = AdjX4;
                    AdjY = AdjY4;
                } else {
                    .print("No valid adjacent position found for (", TargetX, ",", TargetY, ")!");
                }
            }
        }
    }.

/* Orient player to face the object */
+!face_object(ObjX, ObjY) : player(X, Y, Dx, Dy, _) <-
    .print("Facing object at (", ObjX, ",", ObjY, ")");
    .print("Current position: (", X, ",", Y, ")");
    .print("Current direction: (", Dx, ",", Dy, ")");
    .print("Target direction: (", ObjX - X, ",", ObjY - Y, ")");
    if (X < ObjX) { // Object is to the east
        !exec_action(east);
    } else {
        if (X > ObjX) { // Object is to the west
            !exec_action(west);
        } else {
            if (Y < ObjY) { // Object is to the south
                !exec_action(south);
            } else {
                if (Y > ObjY) { // Object is to the north
                    !exec_action(north);
                }
            }
        }
    }.


+!navigate(GoalX, GoalY) : player(X, Y, _, _, _) <-
    .print("[Navigation] Starting navigation from (", X, ", ", Y, ") to (", GoalX, ", ", GoalY, ")");
    !bfs_search(X, Y, GoalX, GoalY, Path);
    .print("[Navigation] Path found: ", Path);
    !follow_path(Path);
    DirX = GoalX - X;
    DirY = GoalY - Y;
    if (DirX > 0) {
        !exec_action(east);
    } else {
        if (DirX < 0) {
            !exec_action(west);
        }
    }
    if (DirY > 0) {
        !exec_action(south);
    } else {
        if (DirY < 0) {
            !exec_action(north);
        }
    }.

+!bfs_search(StartX, StartY, GoalX, GoalY, Path) : true <-
    .print("[Pathfinding] Finding path from (", StartX, ", ", StartY, ") to (", GoalX, ", ", GoalY, ")");
    .queue.create(Q);
    +dist(StartX,StartY,0);
    +prev(StartX,StartY,-1,-1);
    .queue.add(Q, pos(StartX, StartY));
    while(.queue.remove(Q,H)) {
        .print("[Pathfinding] Processing node: ", H);
        H = pos(X,Y);
        ?dist(X,Y,D);
        if(not dist(X+1,Y,_) & not cell(_, X+1,Y)) {
            .queue.add(Q, pos(X+1, Y));
            +dist(X+1,Y,D+1);
            +prev(X+1,Y,X,Y);
        }
        if(not dist(X-1,Y,_) & not cell(_, X-1,Y)) {
            .queue.add(Q, pos(X-1, Y));
            +dist(X-1,Y,D+1);
            +prev(X-1,Y,X,Y);
        }
        if(not dist(X,Y+1,_) & not cell(_, X,Y+1)) {
            .queue.add(Q, pos(X, Y+1));
            +dist(X,Y+1,D+1);
            +prev(X,Y+1,X,Y);
        }
        if(not dist(X,Y-1,_) & not cell(_, X,Y-1)) {
            .queue.add(Q, pos(X, Y-1));
            +dist(X,Y-1,D+1);
            +prev(X,Y-1,X,Y);
        }
    };
    
    if(not dist(GoalX,GoalY,_)) {
        .print("[Pathfinding] No path found to (", GoalX, ", ", GoalY, ")");
        Path = [];
    } else {
        !build_path(GoalX, GoalY, [], Path);
    };
    
    // TODO: Delete all the dist and prev beliefs
    
    .print("[Pathfinding] Pathfinding completed").


+!build_path(X, Y, Path, NewPath) : prev(X, Y, -1, -1) <-
     NewPath = Path.

+!build_path(X, Y, Path, NewPath) : prev(X, Y, PX, PY)<-
     AuxPath = [pos(X,Y)|Path];
     !build_path(PX, PY, AuxPath, NewPath).

+!follow_path([]) : player(X,Y,_,_,_) <-
    .print("[Pathfinding] Reached destination at (", X, ", ", Y, ")").

+!follow_path([H | Path]) : player(X,Y,_,_,_) <-
    .print("[Pathfinding] Following path: ", [H | Path]);
    H = pos(GoalX, GoalY);
    DirX = GoalX - X;
    DirY = GoalY - Y;

    if (DirX > 0) {
        !exec_action(east);
    } else {
        if (DirX < 0) {
            !exec_action(west);
        }
    }

    if (DirY > 0) {
        !exec_action(south);
    } else {
        if (DirY < 0) {
            !exec_action(north);
        }
    }
    !follow_path(Path).
     