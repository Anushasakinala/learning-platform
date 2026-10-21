INSERT INTO questions (topic_id, question_text, option_a, option_b, option_c, option_d, correct, difficulty) VALUES
(1, 'Which data type stores true or false?', 'boolean', 'int', 'char', 'String', 'A', 1),
(1, 'What is the default value of an int instance variable?', '0', 'null', '1', 'undefined', 'A', 2),
(1, 'What does the == operator compare for objects?', 'Their contents', 'Their references', 'Their hash codes', 'Their lengths', 'B', 3),
(2, 'What is an object in Java?', 'An instance of a class', 'A method', 'A package', 'A variable type', 'A', 1),
(2, 'Which keyword prevents a method from being overridden?', 'static', 'final', 'abstract', 'private', 'B', 3),
(2, 'An abstract class can:', 'Be instantiated directly', 'Contain abstract methods', 'Not have constructors', 'Not have fields', 'B', 3),
(3, 'Which collection keeps insertion order and allows duplicates?', 'HashSet', 'ArrayList', 'TreeSet', 'HashMap', 'B', 1),
(3, 'Which Map implementation keeps keys sorted?', 'HashMap', 'LinkedHashMap', 'TreeMap', 'Hashtable', 'C', 2),
(3, 'What is the average time complexity of HashMap.get(key)?', 'O(1)', 'O(n)', 'O(log n)', 'O(n log n)', 'A', 3),
(4, 'Which block handles an exception?', 'try', 'catch', 'finally', 'throw', 'B', 1),
(4, 'What does the throws keyword do?', 'Declares exceptions a method may throw', 'Catches exceptions', 'Creates exceptions', 'Ends the program', 'A', 2),
(4, 'try-with-resources requires resources to implement:', 'Runnable', 'Comparable', 'AutoCloseable', 'Serializable', 'C', 3);