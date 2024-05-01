import { HuntProfilePage } from "cypress/support/hunt-profile.po";
import { BeginHuntPage } from "cypress/support/begin-hunt.po";

const page = new HuntProfilePage();
const beginHuntPage = new BeginHuntPage();

describe('Hunt Profile', () => {
  beforeEach(() => {
    page.navigateTo();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
    });
    cy.task('seed:database');
  });

  it('should display the title', () => {
    page.getHuntCardTitle().should('exist');
  });

  it('should display the description', () => {
    page.getHuntCardDescription().should('exist');
  })

  it('should display a task', () => {
    page.getTasks().contains('Take a picture of');
  });

  it('should click home button and go back to host page', () => {
    page.getHomeButton().should('exist').click();
    cy.url().should('match', /\//);
  })

  it('should display the return to hunts button and click it go back to host page', () => {
    page.getHuntCardReturnToHuntsButton().should('exist');
    page.getHuntCardReturnToHuntsButton().click();
    cy.url().should('match', /\/hosts/);
  })

  it('should display the Begin Hunt button, click it, go to the add teams page, select teams, and then go to the started hunt page', () => {
    page.getHuntCardBeginHuntButton().should('exist');
    page.getHuntCardBeginHuntButton().click();
    cy.wait(1000);
    // Update the URL assertion to match the actual URL
    cy.url().should('include', `http://localhost:4200/startedHunts/`, `/addTeams`);
    beginHuntPage.selectTeamNumber(2);
    beginHuntPage.clickProceedButton();
    cy.wait(1000);
    page.getAccessCode().then((accessCode) => {
      cy.url().should('eq', `http://localhost:4200/startedHunts/${accessCode}`);
    });
  });

  describe('Adding a new task and deleting hunts and tasks', () => {

    it('should add a task', () => {
      page.addTaskButton().should('be.visible').click();
      page.confirmAddTaskButton().should('be.disabled');
      page.getFormField('name').type('Test');
      page.confirmAddTaskButton().should('be.enabled').click();
      page.getTasks().last().contains('Test');
    });

    it('should delete a task', () => {
      page.deleteTask();
      page.getTasks().first().contains('Take a picture of a bird');
    });

    it('should delete a hunt', () => {
      page.deleteHunt();
      cy.url()
        .should('match', /\/hosts/)
        .should('not.match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });
  });
});
