CREATE TABLE topics (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  prerequisite_id BIGINT NULL,
  FOREIGN KEY (prerequisite_id) REFERENCES topics(id)
);

CREATE TABLE questions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  topic_id BIGINT NOT NULL,
  question_text VARCHAR(300) NOT NULL,
  option_a VARCHAR(200) NOT NULL,
  option_b VARCHAR(200) NOT NULL,
  option_c VARCHAR(200) NOT NULL,
  option_d VARCHAR(200) NOT NULL,
  correct CHAR(1) NOT NULL,
  difficulty INT NOT NULL DEFAULT 1,
  FOREIGN KEY (topic_id) REFERENCES topics(id)
);

CREATE TABLE lessons (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  topic_id BIGINT NOT NULL,
  title VARCHAR(150) NOT NULL,
  minutes INT NOT NULL,
  lesson_level INT NOT NULL,
  FOREIGN KEY (topic_id) REFERENCES topics(id)
);

INSERT INTO topics (id, name, prerequisite_id) VALUES
(1, 'Java Basics', NULL),
(2, 'Object-Oriented Programming', 1),
(3, 'Collections', 2),
(4, 'Exception Handling', 2);

INSERT INTO questions (topic_id, question_text, option_a, option_b, option_c, option_d, correct, difficulty) VALUES
(1, 'Which keyword declares a constant in Java?', 'const', 'final', 'static', 'fixed', 'B', 1),
(1, 'What is the size of an int in Java?', '8 bits', '16 bits', '32 bits', '64 bits', 'C', 1),
(1, 'Which method is the entry point of a Java program?', 'start()', 'run()', 'main()', 'init()', 'C', 1),
(2, 'Which principle hides internal details of a class?', 'Encapsulation', 'Inheritance', 'Polymorphism', 'Recursion', 'A', 2),
(2, 'Which keyword is used to inherit from a class?', 'implements', 'extends', 'inherits', 'super', 'B', 2),
(2, 'Method overloading means:', 'Same name, different parameters', 'Same method in a subclass', 'Different names', 'Hiding a field', 'A', 2),
(3, 'Which collection does NOT allow duplicates?', 'ArrayList', 'LinkedList', 'HashSet', 'Vector', 'C', 2),
(3, 'Which structure stores key-value pairs?', 'List', 'Set', 'Queue', 'Map', 'D', 2),
(3, 'What is the time complexity of ArrayList.get(i)?', 'O(1)', 'O(n)', 'O(log n)', 'O(n squared)', 'A', 3),
(4, 'Which block always runs, even after an exception?', 'try', 'catch', 'finally', 'throw', 'C', 2),
(4, 'Which of these is a checked exception?', 'NullPointerException', 'IOException', 'ArithmeticException', 'ArrayIndexOutOfBoundsException', 'B', 3),
(4, 'Which keyword manually throws an exception?', 'throws', 'throw', 'raise', 'catch', 'B', 2);

INSERT INTO lessons (topic_id, title, minutes, lesson_level) VALUES
(1, 'Variables, data types and constants', 20, 1),
(1, 'Methods and the main entry point', 15, 2),
(2, 'Classes, objects and encapsulation', 25, 1),
(2, 'Inheritance, overriding and overloading', 30, 2),
(3, 'List, Set and Map basics', 25, 1),
(3, 'Choosing the right collection and its complexity', 30, 2),
(4, 'try, catch and finally', 20, 1),
(4, 'Checked vs unchecked exceptions', 20, 2);