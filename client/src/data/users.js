// Hardcoded users fixture — admin table + coordinator results pick from here
export const users = [
  { id: "u1", name: "Manoj", email: "manoj@nit.edu", rollNo: "25CSB0A53", role: "student", branch: "CSE", batch: "2027", cgpa: 7.8, backlogs: 0, status: "active" },
  { id: "u2", name: "Chakradhar", email: "chakradhar@nit.edu", rollNo: "25CSB0A76", role: "student", branch: "ECE", batch: "2027", cgpa: 7.2, backlogs: 0, status: "active" },
  { id: "u3", name: "Raghu", email: "raghu@nit.edu", rollNo: "25CSB0A80", role: "student", branch: "CSE", batch: "2027", cgpa: 6.9, backlogs: 1, status: "active" },
  { id: "u4", name: "Ram Charan", email: "ramcharan@nit.edu", rollNo: "25CSB0A46", role: "student", branch: "IT", batch: "2027", cgpa: 8.1, backlogs: 0, status: "active" },
  { id: "u5", name: "Sneha", email: "sneha@nit.edu", rollNo: "25CSB0A61", role: "student", branch: "CSE", batch: "2027", cgpa: 8.4, backlogs: 0, status: "active" },
  { id: "u6", name: "Arjun", email: "arjun@nit.edu", rollNo: "25CSB0A99", role: "student", branch: "MECH", batch: "2027", cgpa: 6.5, backlogs: 2, status: "blocked" },
  { id: "u7", name: "Prof. Kumar", email: "kumar@nit.edu", rollNo: "—", role: "coordinator", branch: "—", batch: "—", cgpa: null, backlogs: null, status: "active" },
  { id: "u8", name: "Ms. Priya", email: "priya@nit.edu", rollNo: "—", role: "coordinator", branch: "—", batch: "—", cgpa: null, backlogs: null, status: "active" },
  { id: "u9", name: "Manoj", email: "admin@nit.edu", rollNo: "—", role: "admin", branch: "—", batch: "—", cgpa: null, backlogs: null, status: "active" },
];

export const students = users.filter((u) => u.role === "student");
