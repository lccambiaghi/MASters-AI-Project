// Planner performs the POP Algorithm
    // agenda: set of open preconditions of the plan
        // open precondition is a pair <Q, Ai> where Q is a conjunct of precondition of Ai
    // plan: POPlan 
    // set of threatened links

// initialise null plan
// creates empty plan, adds Start and Finish pseudo actions to the plan

// POP algorithm (POPlan, agenda, newInstanceOfAction=null):
//{
// if agenda is empty, return POPlan

// Goal selection: pick (not choose) one open precondition in the agenda

// Action selection: choose action that achieves open precondition
    // is there an already existing action that achieves it?
        // update links
        // update ordering constraints
    // else:
        // is possible to create new instance of action(s)?
            // update actions
            // update ordering constraints
        // else:
            // failure

// Update agenda: agenda = agenda - open precondition <Q, A>
// new instance of action?
    // for each conjunct of its precondition, add <Qi, A>      

// Causal Link Protection

// Recursive call: POP(plan', agenda', newInstanceOfAction)
//}

// linearisation of plan using topological research algorithm