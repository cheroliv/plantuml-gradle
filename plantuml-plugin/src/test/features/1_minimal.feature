noinspection CucumberUndefinedStep
@cucumber @plantuml
Feature: Minimal Plantuml configuration

  Scenario: Canary
    Given a new Plantuml project
    When I am executing the task 'tasks'
    Then the build should succeed
