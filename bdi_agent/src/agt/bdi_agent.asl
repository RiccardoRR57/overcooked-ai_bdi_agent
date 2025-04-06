
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

+player1(X,Y,Dx,Dy,Holding) : true <- .print("player1: ", X, ", ", Y, ", ", Dx, ", ", Dy, ", ", Holding).

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
+!go_towards(Object) : player1(StartX, StartY, _, _, _) & cell(Object, EndX, EndY) <-
    .print("Cerco il percorso da (", StartX, ",", StartY, ") a (", EndX, ",", EndY, ")");
    find_path([(StartX, StartY)], [(StartX, StartY)], [(StartX, StartY)], EndX, EndY, Object).

/* Piano ricorsivo per la ricerca del percorso */
+!find_path([Pos | _], Path, Visited, EndX, EndY, Object) : adjacent(Pos, EndX, EndY) & facing(Pos, EndX, EndY) <-
    .print("Percorso trovato:", Path),
    .print("Player rivolto verso l'oggetto");

+!find_path([Pos | Rest], Path, Visited, EndX, EndY, Object) : \+adjacent(Pos, EndX, EndY) <-
    get_neighbors(Pos, Visited, Neighbors);
    add_neighbors(Neighbors, Path, Visited, NewQueue, NewVisited);
    find_path(NewQueue ++ Rest, Path, NewVisited, EndX, EndY, Object).

/* Regole per ottenere i vicini validi */
get_neighbors((Row, Col), Visited, Neighbors) :-
    height(Rows), width(Cols);
    .findall(Neighbor, valid_neighbor(Rows, Cols, (Row, Col), Visited, Neighbor), Neighbors).

valid_neighbor(Rows, Cols, (Row, Col), Visited, (NRow, NCol)) :-
    NRow = Row + DR,
    NCol = Col + DC,
    .member((DR, DC), [(0, 1), (0, -1), (1, 0), (-1, 0)]), /* Direzioni */
    NRow >= 0, NRow < Rows,
    NCol >= 0, NCol < Cols,
    \+ cell(_, NRow, NCol), /* Cella libera */
    \+ .member((NRow, NCol), Visited). /* Non visitata */

/* Regole per aggiungere i vicini alla coda */
add_neighbors([], _, Visited, [], Visited).

add_neighbors([Neighbor | Rest], Path, Visited, [Neighbor | NewQueue], NewVisited) :-
    NewPath = Path ++ [Neighbor],
    NewVisited = Visited ++ [Neighbor],
    add_neighbors(Rest, Path, NewQueue, NewVisited).

/* Regole per la cella adiacente */
adjacent((X, Y), EndX, EndY) :-
    (X = EndX + 1 & Y = EndY) | (X = EndX - 1 & Y = EndY) | (X = EndX & Y = EndY + 1) | (X = EndX & Y = EndY - 1).

/* Regole per la direzione */
facing((X,Y),EndX,EndY) :-
    player1(X,Y,Dx,Dy,_),
    (EndX - X = Dx),
    (EndY - Y = Dy).