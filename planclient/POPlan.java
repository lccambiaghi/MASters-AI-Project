// POPlan is a tuple <A, O, L>
    // A = set of actions
    // O = set of ordering constraints
        // ordering constraint: A1 < A2
    // L = set of causal links
        // casual link: A1 -eff-> A2

// POPlan is representable with a directed aciclyc graph (DAG) of actions

// POPlan is complete when its open preconditions set is empty