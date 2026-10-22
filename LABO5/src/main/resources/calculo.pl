% tuProlog no declara el signo positivo unario entre sus operadores por defecto.
:- op(200, fy, '+').

% Cada término se representa como t(Exponente, Coeficiente).
% Por ejemplo: 5*x^2 se convierte en t(2, 5).

% Operaciones nuevas: reciben una suma o resta completa desde Java.
sumar(Var, A+B, R) :-
    contiene_variable(Var, A+B),
    polinomio(Var, A, TA),
    polinomio(Var, B, TB),
    append(TA, TB, Terminos),
    reducir(Var, Terminos, R).

restar(Var, A-B, R) :-
    contiene_variable(Var, A-B),
    polinomio(Var, A, TA),
    polinomio(Var, B, TB),
    escalar(TB, -1, Negados),
    append(TA, Negados, Terminos),
    reducir(Var, Terminos, R).

% Exigir una variable evita convertir el ejercicio en una calculadora numérica.
contiene_variable(Var, E) :- E == Var, !.
contiene_variable(Var, E) :-
    compound(E), E =.. [_|Argumentos],
    member(Argumento, Argumentos), contiene_variable(Var, Argumento), !.

% Convertir constantes, potencias, signos y sumas/restas en listas de términos.
polinomio(_, K, [t(0, K)]) :- number(K), !.
polinomio(Var, E, [t(1, 1)]) :- E == Var, !.
polinomio(Var, Var^N, [t(N, 1)]) :- integer(N), N >= 0, !.
polinomio(Var, +A, T) :- !, polinomio(Var, A, T).
polinomio(Var, -A, T) :- !, polinomio(Var, A, TA), escalar(TA, -1, T).
polinomio(Var, A+B, T) :- !,
    polinomio(Var, A, TA), polinomio(Var, B, TB), append(TA, TB, T).
polinomio(Var, A-B, T) :- !,
    polinomio(Var, A, TA), polinomio(Var, B, TB),
    escalar(TB, -1, Negados), append(TA, Negados, T).
polinomio(Var, K*A, T) :- number(K), !,
    polinomio(Var, A, TA), escalar(TA, K, T).
polinomio(Var, A*K, T) :- number(K), !,
    polinomio(Var, A, TA), escalar(TA, K, T).
polinomio(Var, A/K, T) :- number(K), K =\= 0,
    polinomio(Var, A, TA), Factor is 1.0/K, escalar(TA, Factor, T).

escalar([], _, []).
escalar([t(N, C)|Ts], Factor, [t(N, Nuevo)|Rs]) :-
    Nuevo is C*Factor, escalar(Ts, Factor, Rs).

reducir(Var, Terminos, R) :-
    agrupar(Terminos, Agrupados), expresion(Var, Agrupados, R).

% Insertar en orden descendente, sumando solo coeficientes del mismo grado.
agrupar([], []).
agrupar([T|Ts], R) :- agrupar(Ts, Parcial), insertar(T, Parcial, R).

insertar(t(_, C), Ts, Ts) :- C =:= 0, !.
insertar(T, [], [T]) :- !.
insertar(t(N, C), [t(N, D)|Ts], R) :- !,
    Total is C+D,
    (Total =:= 0 -> R = Ts ; R = [t(N, Total)|Ts]).
insertar(t(N, C), [t(M, D)|Ts], [t(N, C), t(M, D)|Ts]) :- N > M, !.
insertar(T, [H|Ts], [H|Rs]) :- insertar(T, Ts, Rs).

% Reconstruir sin coeficientes cero, sin 1*x y sin potencias x^1 o x^0.
expresion(_, [], 0).
expresion(Var, [t(N, C)|Ts], R) :-
    monomio(Var, N, C, Inicial),
    completar(Var, Ts, Inicial, R).

completar(_, [], R, R).
completar(Var, [t(N, C)|Ts], Acumulado, R) :-
    Magnitud is abs(C), monomio(Var, N, Magnitud, T),
    (C < 0 -> Siguiente = Acumulado-T ; Siguiente = Acumulado+T),
    completar(Var, Ts, Siguiente, R).

monomio(_, 0, C, C) :- !.
monomio(Var, N, C, T) :-
    (N =:= 1 -> Potencia = Var ; Potencia = Var^N),
    (C =:= 1 -> T = Potencia ; C =:= -1 -> T = -Potencia ; T = C*Potencia).

% Reglas de cálculo de la guía, con linealidad y simplificación de polinomios.
d(Var, E, R) :- derivada(Var, E, D), simplificar_si_polinomio(Var, D, R).
s(Var, E, R) :- integral(Var, E, I), simplificar_si_polinomio(Var, I, R).

simplificar_si_polinomio(Var, E, R) :- polinomio(Var, E, Ts), !, reducir(Var, Ts, R).
simplificar_si_polinomio(_, E, E).

derivada(_, K, 0) :- number(K), !.
derivada(Var, Var, 1) :- atom(Var), !.
derivada(Var, Var^N, 0) :- number(N), N =:= 0, !.
derivada(Var, Var^N, N*Var^M) :- number(N), !, M is N-1.
derivada(Var, log(Var), 1/Var) :- atom(Var), !.
derivada(Var, A+B, DA+DB) :- !, derivada(Var, A, DA), derivada(Var, B, DB).
derivada(Var, A-B, DA-DB) :- !, derivada(Var, A, DA), derivada(Var, B, DB).
derivada(Var, -A, -DA) :- !, derivada(Var, A, DA).
derivada(Var, +A, DA) :- !, derivada(Var, A, DA).
derivada(Var, K*A, K*DA) :- number(K), !, derivada(Var, A, DA).
derivada(Var, A*K, K*DA) :- number(K), !, derivada(Var, A, DA).
derivada(Var, A/K, DA/K) :- number(K), K =\= 0, !, derivada(Var, A, DA).
derivada(Var, K^Var, log(K)*K^Var) :- number(K), K > 0.

integral(Var, K, K*Var) :- number(K), !.
integral(Var, Var, Var^2/2) :- atom(Var), !.
integral(Var, Var^N, log(Var)) :- number(N), N =:= -1, !.
integral(Var, Var^N, Var^M/M) :- number(N), !, M is N+1.
integral(Var, 1/Var, log(Var)) :- atom(Var), !.
integral(Var, A+B, IA+IB) :- !, integral(Var, A, IA), integral(Var, B, IB).
integral(Var, A-B, IA-IB) :- !, integral(Var, A, IA), integral(Var, B, IB).
integral(Var, -A, -IA) :- !, integral(Var, A, IA).
integral(Var, +A, IA) :- !, integral(Var, A, IA).
integral(Var, K*A, K*IA) :- number(K), !, integral(Var, A, IA).
integral(Var, A*K, K*IA) :- number(K), !, integral(Var, A, IA).
integral(Var, A/K, IA/K) :- number(K), K =\= 0, !, integral(Var, A, IA).
integral(Var, K^Var, K^Var/log(K)) :- number(K), K > 0, K =\= 1.
