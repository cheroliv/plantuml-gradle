Feature: Syntax Validation

  Scenario: Validate correct PlantUML file
    Given a valid PlantUML file "valid.puml" with content "@startuml\nactor User\n@enduml"
    When I run validatePlantumlSyntax task with file "valid.puml"
    Then the syntax should be reported as valid

  Scenario: Validate invalid PlantUML file
    Given an invalid PlantUML file "invalid.puml" with content "invalid plantuml code"
    When I run validatePlantumlSyntax task with file "invalid.puml"
    Then the syntax should be reported as invalid
    And error details should be provided

  @wip
  Scenario: Validate empty PlantUML file
    Given an invalid PlantUML file "empty.puml" with content ""
    When I run validatePlantumlSyntax task with file "empty.puml"
    Then the syntax should be reported as invalid