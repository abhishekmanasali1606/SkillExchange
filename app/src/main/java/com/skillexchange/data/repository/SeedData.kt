package com.skillexchange.data.repository

import com.skillexchange.data.model.Post

// Optional: seed some posts on first launch if Firestore is empty
val SEED_POSTS = listOf(
    Post(
        id = "seed1",
        userId = "seed_user1",
        title = "Leaking roof needs urgent fixing",
        description = "My roof has been leaking badly for weeks. Need an experienced roofer urgently! Can offer plumbing work in return.",
        skillRequired = "Roofer",
        timestamp = System.currentTimeMillis() - 86400000
    ),
    Post(
        id = "seed2",
        userId = "seed_user2",
        title = "Electrical wiring issue at home",
        description = "Main board needs rewiring. Safety concern — sparks sometimes visible. Looking for a skilled electrician.",
        skillRequired = "Electrician",
        timestamp = System.currentTimeMillis() - 3600000
    ),
    Post(
        id = "seed3",
        userId = "seed_user3",
        title = "Water pump motor not working",
        description = "Our water pump motor stopped working. Need someone to diagnose and fix. Happy to do carpentry work in exchange.",
        skillRequired = "Electrician",
        timestamp = System.currentTimeMillis() - 7200000
    ),
    Post(
        id = "seed4",
        userId = "seed_user4",
        title = "Kitchen wooden shelves needed",
        description = "Want 3 sturdy wooden shelves installed in my kitchen. Will paint your walls or furniture in exchange!",
        skillRequired = "Carpenter",
        timestamp = System.currentTimeMillis() - 18000000
    )
)
