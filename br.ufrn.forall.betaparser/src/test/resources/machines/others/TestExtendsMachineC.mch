MACHINE TestExtendsMachineC

EXTENDS TestExtendsMachineB

SETS
 SETC={ee,ff}

CONSTANTS constantC

PROPERTIES
 constantC : SETC

VARIABLES varC

INVARIANT varC : INT

INITIALISATION varC := 1

OPERATIONS
  opC(yy) = PRE yy : SETC THEN skip END
END

