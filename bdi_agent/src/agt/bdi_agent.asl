+timestep(0) : true <- 
    .print("timestep: 0");
    !start.

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

+!start : player(X,Y,Dx,DY,_) <- 
    !reachability(X,Y);
    !cook.

+!reachability(StartX,StartY) : true <- 
    .queue.create(Q);
    +dist(StartX,StartY,0);
    +prev(StartX,StartY,-1,-1);
    .queue.add(Q, pos(StartX, StartY));
    while(.queue.remove(Q,H)) {
        .print("[Reachability] Processing node: ", H);
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

    .set.create(Reachable);
    .findall(pos(X,Y),dist(X,Y,_),L);

    for(.member(V,L)) {
        V=pos(X,Y);
        if(cell(Object1, X+1, Y)) {
            .set.add(Reachable, Object1);
        }
        if(cell(Object2, X-1, Y)) {
            .set.add(Reachable, Object2);
        }
        if(cell(Object3, X, Y+1)) {
            .set.add(Reachable, Object3);
        }
        if(cell(Object4, X, Y-1)) {
            .set.add(Reachable, Object4);
        };
    };

    .abolish(dist(_,_,_));
    .abolish(prev(_,_,_,_));

    +reachable(Reachable);
    .print("[Reachability] Reachable cells: ", Reachable).

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
+!go_towards(Object) : reachable(R) & .member(Object, R) & player(StartX, StartY,_,_,_)<-
    !bfs_search(StartX, StartY, Object, Path);
    !follow_path(Path);
    !face_object(Object).

+!go_towards(Object) : object(Object, EndX, EndY) & player(StartX, StartY,_,_,_)<-
    .print("Object ", Object, " found");
    !bfs_search(StartX, StartY, Object, Path);
    !follow_path(Path);
    !face_object(Object).

+!go_towards(Object) : player(StartX, StartY,_,_,_) <-
    .print("Cannot find location for ", Object);
    !exec_action(wait);
    !go_towards(Object).

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
+!face_object(Object) : player(X, Y, Dx, Dy, _) <-    
    if(cell(Object, X+1, Y) | object(Object, X+1, Y)) {
        !exec_action(east);
    }
    if(cell(Object, X-1, Y) | object(Object, X-1, Y)) {
        !exec_action(west);
    }
    if(cell(Object, X, Y+1) | object(Object, X, Y+1)) {
        !exec_action(south);
    }
    if(cell(Object, X, Y-1) | object(Object, X, Y-1)) {
        !exec_action(north);
    }.

+!bfs_search(StartX, StartY, Object, Path) : true <-
    .print("[Pathfinding] Finding path from (", StartX, ", ", StartY, ") to (", Object, ")");
    .queue.create(Q);
    +dist(StartX,StartY,0);
    +prev(StartX,StartY,-1,-1);
    .queue.add(Q, pos(StartX, StartY));
    while(.queue.remove(Q,H)) {
        .print("[Pathfinding] Processing node: ", H);
        H = pos(X,Y);
        ?dist(X,Y,D);
        if(not dist(X+1,Y,_) & not cell(_, X+1,Y) & not other_player(X+1,Y,_,_,_)) {
            .queue.add(Q, pos(X+1, Y));
            +dist(X+1,Y,D+1);
            +prev(X+1,Y,X,Y);
        }
        if(not dist(X-1,Y,_) & not cell(_, X-1,Y) & not other_player(X-1,Y,_,_,_)) {
            .queue.add(Q, pos(X-1, Y));
            +dist(X-1,Y,D+1);
            +prev(X-1,Y,X,Y);
        }
        if(not dist(X,Y+1,_) & not cell(_, X,Y+1) & not other_player(X,Y+1,_,_,_)) {
            .queue.add(Q, pos(X, Y+1));
            +dist(X,Y+1,D+1);
            +prev(X,Y+1,X,Y);
        }
        if(not dist(X,Y-1,_) & not cell(_, X,Y-1) & not other_player(X,Y-1,_,_,_)) {
            .queue.add(Q, pos(X, Y-1));
            +dist(X,Y-1,D+1);
            +prev(X,Y-1,X,Y);
        }
    };
    
    .findall(pos(X,Y),cell(Object,X,Y) | object(Object, X, Y),L);

    !choose_best(L, -1, -1, BestX, BestY);

    if(not dist(BestX,BestY,_)) {
        .print("[Pathfinding] No path found to (", Object, ")");
        !exec_action(random);
        .abolish(dist(_,_,_));
        .abolish(prev(_,_,_,_));
        !go_towards(Object);
    } else {
        .print("[Pathfinding] Building path to (", BestX, ", ", BestY, ")");
        !build_path(BestX, BestY, [], Path);
    };
    
    .abolish(dist(_,_,_));
    .abolish(prev(_,_,_,_));
    
    .print("[Pathfinding] Pathfinding completed").

+!choose_best([], CurrX, CurrY, BestX, BestY) : true <-
    .print("[Pathfinding] Found best position: (", CurrX, ", ", CurrY, ")");
    BestX = CurrX;
    BestY = CurrY.

+!choose_best([H | T], CurrX, CurrY, BestX, BestY) : dist(CurrX,CurrY,CurrDist) <-
    H = pos(X,Y);
    !find_adjacent_position(X,Y,AdjX,AdjY);
    if(dist(AdjX,AdjY,D)) {
        if (D < CurrDist) {
            !choose_best(T, AdjX, AdjY, BestX, BestY);
        }
        else {
            !choose_best(T, CurrX, CurrY, BestX, BestY);
        }
    }
    else {
        .print("[Pathfinding] No valid adjacent position found for (", X, ",", Y, ")");
        .print("[Pathfinding] Choosing best position from remaining nodes");
        .print("[Pathfinding] Remaining nodes: ", T);
        .print("[Pathfinding] Current best position: (", CurrX, ", ", CurrY, ")");
        !choose_best(T, CurrX, CurrY, BestX, BestY);
    }.

+!choose_best([H | T], _, _, BestX, BestY) : true <-
    H = pos(X,Y);
    !find_adjacent_position(X,Y,AdjX,AdjY);
    if (dist(AdjX,AdjY,D)) {
        !choose_best(T, AdjX, AdjY, BestX, BestY);
    }
    else {
        .print("[Pathfinding] No valid adjacent position found for (", X, ",", Y, ")");
        .print("[Pathfinding] Choosing best position from remaining nodes");
        .print("[Pathfinding] Remaining nodes: ", T);
        .print("[Pathfinding] Current best position: (-1, -1)");
        !choose_best(T, -1, -1, BestX, BestY);
    }.

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
    ?player(NewX,NewY,_,_,_);
    if(NewX == GoalX & NewY == GoalY) {
        !follow_path(Path);
    } else {
        !follow_path([H | Path]);
    }.
     