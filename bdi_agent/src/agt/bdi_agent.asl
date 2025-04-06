+timestep(0) : true <- 
    .print("timestep: 0");
    !cook.

+!exec_action(A) : timestep(N) 
    <-  A;
        .wait(timestep(N+1)).

+width(W) : true <- .print("width: ", W).

+height(H) : true <- .print("height: ", H).

+cell(T,X,Y) : true <- .print("cell: ", T, ", ", X, ", ", Y).

+object(T,X,Y) : true <- .print("object: ", T, ", ", X, ", ", Y).

+player1(X,Y,Dx,Dy,Holding) : true <- 
    .print("player1: ", X, ", ", Y, ", ", Dx, ", ", Dy, ", ", Holding);
    -+my_pos(X,Y);
    -+my_dir(Dx,Dy);
    -+my_holding(Holding).

+player2(X,Y,Dx,Dy,Holding) : true <- .print("player2: ", X, ", ", Y, ", ", Dx, ", ", Dy, ", ", Holding).

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
    !go_towards(dish);
    !exec_action(interact);
    !go_towards(pot);
    !exec_action(interact);
    !go_towards(serve);
    !exec_action(interact).

/* Piano principale */
+!go_towards(Object) : cell(Object, EndX, EndY)  & my_pos(StartX, StartY)<-
    .print("Cerco il percorso da (", StartX, ",", StartY, ") a (", EndX, ",", EndY, ")");
    !navigate_to(EndX, EndY).

+!go_towards(Object) : cell(onion, EndX, EndY) <- 
    .print("Ho trovato l'oggetto ", Object, "nella cella ", Endx, EndY ).

+!go_towards(Object) : my_pos(StartX, StartY) <-
    .print("sono nella cella (", StartX, ",", StartY, ")");
    .print("non so dove andare per _____", Object, "_____").

// Navigation plan
+!navigate_to(TargetX, TargetY) : my_pos(TargetX, TargetY) <- 
    .print("Arrivato a destinazione (", TargetX, ",", TargetY, ")").

+!navigate_to(TargetX, TargetY) : my_pos(X, Y) <- 
    // Determine direction to move
    if (X < TargetX) {
        !exec_action(right);
    } else {
        if (X > TargetX) {
            !exec_action(left);
        } else {
            if (Y < TargetY) {
                !exec_action(down);
            } else {
                !exec_action(up);
            }
        }
    }
    // Continue navigation until destination
    !navigate_to(TargetX, TargetY).
