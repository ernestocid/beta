spec_trans(root,'$initialise_machine',0).
spec_trans(0,'op1(0,0)',0).
spec_max_reached_for_node(root).
spec_max_reached_for_node(0).
spec_not_all_transitions_added(_) :-
        fail.
spec_completely_explored :-
        fail.
