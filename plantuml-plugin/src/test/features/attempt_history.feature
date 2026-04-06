Feature: Attempt History Tracking

#  Scenario: Track successful diagram generation with corrections
#    Given a prompt file "tracking-test.prompt" with content "Create a simple user diagram"
#    And a mock LLM that returns an invalid PlantUML diagram on first attempt
#    And a mock LLM that returns a valid PlantUML diagram on second attempt
#    When I run processPlantumlPrompts task
#    Then attempt history should be tracked with 2 entries
#    And the first entry should indicate syntax error
#    And the second entry should indicate success
#    And a valid diagram should be generated
#    And a PNG image should be created

#  Scenario: Archive history after max iterations with no success
#    Given a prompt file "archive-test.prompt" with content "Create a complex diagram"
#    And a mock LLM that always returns invalid PlantUML diagrams
#    When I run processPlantumlPrompts task with max 5 iterations
#    Then attempt history should be archived with 5 entries
#    And no diagram should be generated
#    And the prompt file should be deleted

#  Scenario: Successful generation after multiple corrections
#    Given a prompt file "multi-correction-test.prompt" with content "Create an architecture diagram"
#    And a mock LLM that returns invalid PlantUML diagrams for first 3 attempts
#    And a mock LLM that returns a valid PlantUML diagram on fourth attempt
#    When I run processPlantumlPrompts task
#    Then attempt history should be tracked with 4 entries
#    And the first three entries should indicate syntax errors
#    And the fourth entry should indicate success
#    And a valid diagram should be generated
#    And validation feedback should be saved