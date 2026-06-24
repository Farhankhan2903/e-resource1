package com.eresource.solution.data.repository

import com.eresource.solution.data.models.Worker

object DemoTechnicianRepository {
    
    val approvedTechnicians = listOf(
        "Rahul Sharma", "Amit Verma", "Vikram Singh", "Sandeep Kumar", "Rajesh Patel",
        "Akash Yadav", "Rohan Gupta", "Prakash Joshi", "Arjun Mehta", "Deepak Sharma",
        "Mohit Jain", "Kunal Desai", "Nitin Mishra", "Manish Singh", "Gaurav Tiwari",
        "Harsh Patel", "Vivek Kumar", "Rohit Saini", "Aditya Sharma", "Sunil Chauhan",
        "Ravi Kumar", "Naveen Reddy", "Shubham Gupta", "Pankaj Yadav", "Vinay Sharma"
    ).mapIndexed { index, name ->
        Worker(
            worker_id = 100 + index,
            shop_name = name,
            shop_addr = "Tech Street, India",
            type = if (index % 2 == 0) "Electrician" else "Computer",
            contact_no = "+91 90000 12345",
            verified = 1,
            avg_rating = (42 + (index % 8)).toFloat() / 10f,
            total_reviews = 50 + (index * 5)
        )
    }

    val pendingTechnicians = listOf(
        "Ankit Verma", "Tarun Kumar", "Chirag Shah", "Ashish Yadav", "Karthik Reddy"
    ).mapIndexed { index, name ->
        Worker(
            worker_id = 200 + index,
            shop_name = name,
            shop_addr = "Awaiting Verification",
            type = "Technician",
            contact_no = "+91 80000 00000",
            verified = null,
            avg_rating = 0f,
            total_reviews = 0
        )
    }

    val rejectedTechnicians = listOf(
        "Lokesh Kumar", "Ritesh Sharma", "Manoj Patel", "Vikas Gupta", "Dinesh Singh"
    ).mapIndexed { index, name ->
        Worker(
            worker_id = 300 + index,
            shop_name = name,
            shop_addr = "Verification Failed",
            type = "Technician",
            contact_no = "+91 70000 00000",
            verified = 0,
            avg_rating = 0f,
            total_reviews = 0
        )
    }

    val allDemoWorkers = approvedTechnicians + pendingTechnicians + rejectedTechnicians
}
