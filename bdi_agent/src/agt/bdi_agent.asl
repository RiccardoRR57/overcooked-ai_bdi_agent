
+timestep(0) : true <- 
    .print("timestep: 0");
    !cook.

+!cook : true <- !exec_action(north);
                !exec_action(south);
                !exec_action(east);
                !exec_action(west).


+!exec_action(A) : timestep(N) 
    <-  A;
        .wait(timestep(N+1)).

+width(W) : true <- .print("width: ", W).

+height(H) : true <- .print("height: ", H).

+cell(T,X,Y) : true <- .print("cell: ", T, ", ", X, ", ", Y).

+object(T,X,Y) : true <- .print("object: ", T, ", ", X, ", ", Y).

+player1(X,Y,Dx,Dy,Holding) : true <- .print("player1: ", X, ", ", Y, ", ", Dx, ", ", Dy, ", ", Holding).

+player2(X,Y,Dx,Dy,Holding) : true <- .print("player2: ", X, ", ", Y, ", ", Dx, ", ", Dy, ", ", Holding).

+timestep(N) : true <- .print("timestep: ", N).

+order(I1,I2,I3) : true <- .print("order: ", I1, ", ", I2, ", ", I3).

+bonus_order(I1,I2,I3) : true <- .print("bonus order: ", I1, ", ", I2, ", ", I3).

