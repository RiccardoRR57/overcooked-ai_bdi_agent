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
    !astar_navigate(EndX, EndY).

+!go_towards(Object) : player(StartX, StartY,_,_,_) <-
    .print("sono nella cella (", StartX, ",", StartY, ")");
    .print("non so dove andare per ", Object).

// --- A* Navigation for Jason ---

+!astar_navigate(GoalX, GoalY) : player(StartX, StartY,_,_,_) <-
    !clear_astar;
    G = 0;
    H = math.abs(StartX - GoalX) + math.abs(StartY - GoalY);
    +open(StartX, StartY, G, H, -1, -1);
    !astar_search(GoalX, GoalY).

+!clear_astar <-
    -open(_,_,_,_,_,_);
    -closed(_,_); 
    -path(_,_).

+!astar_search(GoalX, GoalY) : open(X,Y,G,H,ParentX,ParentY) & not closed(GoalX,GoalY) <-
    !select_best_open(X1,Y1,G1,H1,ParentX1,ParentY1);
    +closed(X1,Y1);
    -open(X1,Y1,G1,H1,ParentX1,ParentY1);
    if (X1 == GoalX & Y1 == GoalY) {
        !reconstruct_path(X1,Y1);
    } else {
        !expand_neighbors(X1,Y1,G1,GoalX,GoalY);
        !astar_search(GoalX, GoalY);
    }.

+!select_best_open(X,Y,G,H,ParentX,ParentY) : open(X,Y,G,H,ParentX,ParentY) & not (open(X2,Y2,G2,H2,_,_) & (G2+H2) < (G+H)).

+!expand_neighbors(X,Y,G,GoalX,GoalY) <-
    !try_astar_neighbor(X,Y,G,GoalX,GoalY,1,0);   // east
    !try_astar_neighbor(X,Y,G,GoalX,GoalY,-1,0);  // west
    !try_astar_neighbor(X,Y,G,GoalX,GoalY,0,1);   // south
    !try_astar_neighbor(X,Y,G,GoalX,GoalY,0,-1).  // north

+!try_astar_neighbor(X,Y,G,GoalX,GoalY,DX,DY) <-
    NX = X + DX;
    NY = Y + DY;
    .print("[A*] Considering neighbor (", NX, ",", NY, ")");
    if (NX < 0 | NY < 0) {
        .print("[A*] Skipping invalid neighbor (", NX, ",", NY, ")");
    }
    if (NX >= 0 & NY >= 0) {
        if (cell(_,NX,NY)) {
            .print("[A*] Blocked by cell at (", NX, ",", NY, ")");
        }
        if (not cell(_,NX,NY)) {
            if (object(_,NX,NY)) {
                .print("[A*] Blocked by object at (", NX, ",", NY, ")");
            }
            if (not object(_,NX,NY)) {
                if (closed(NX,NY)) {
                    .print("[A*] Already closed (", NX, ",", NY, ")");
                }
                if (not closed(NX,NY)) {
                    if (open(NX,NY,_,_,_,_)) {
                        .print("[A*] Already open (", NX, ",", NY, ")");
                    }
                    if (not open(NX,NY,_,_,_,_)) {
                        G1 = G + 1;
                        H1 = math.abs(NX - GoalX) + math.abs(NY - GoalY);
                        .print("[A*] Adding open (", NX, ",", NY, ") with G=", G1, " H=", H1, " parent=(", X, ",", Y, ")");
                        +open(NX,NY,G1,H1,X,Y);
                    }
                }
            }
        }
    }.

+!reconstruct_path(X,Y) : closed(X,Y) & open(_,_,_,_,PX,PY) & not (PX == -1 & PY == -1) <-
    +path(X,Y);
    !reconstruct_path(PX,PY).
+!reconstruct_path(X,Y) : closed(X,Y) & open(_,_,_,_,PX,PY) & (PX == -1 & PY == -1) <-
    +path(X,Y);
    !execute_path.

+!execute_path : path(X,Y) & player(X,Y,_,_,_) <-
    .print("[A*] Arrived at destination (", X, ",", Y, ")");
    -path(X,Y).
+!execute_path : path(X,Y) & not player(X,Y,_,_,_) <-
    !move_towards(X,Y);
    -path(X,Y);
    !execute_path.

+!move_towards(X,Y) : player(CX,CY,_,_,_) & CX < X <-
    !exec_action(east).

+!move_towards(X,Y) : player(CX,CY,_,_,_) & CX > X <-
    !exec_action(west).

+!move_towards(X,Y) : player(CX,CY,_,_,_) & CY < Y <-
    !exec_action(south).

+!move_towards(X,Y) : player(CX,CY,_,_,_) & CY > Y <-
    !exec_action(north).
