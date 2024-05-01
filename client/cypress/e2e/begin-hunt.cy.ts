import { BeginHuntPage } from "cypress/support/begin-hunt.po";

const page = new BeginHuntPage();

describe('Begin Hunt', () => {
  beforeEach(() => {
    page.navigateTo();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
    });
    cy.task('seed:database');
  });

  it('should click the begin hunt and navigate to the right access code page', () => {
    page.beginHuntButton().should('exist');
    page.beginHuntButton().click();
    cy.wait(2000);
    page.selectTeamNumber(5);
    page.clickProceedButton();
    cy.wait(1000);
    page.getAccessCode().then((accessCode) => {
      cy.wait(2000);
      cy.url().should('eq', `http://localhost:4200/startedHunts/${accessCode}`);
    });
  })

  it('should click the Begin Hunt button again to start the hunt', () => {
    page.beginHuntButton().should('exist');
    page.beginHuntButton().click();
    cy.wait(2000);
    page.selectTeamNumber(5);
    page.clickProceedButton();
    cy.wait(1000);
    page.getAccessCode().then((accessCode) => {
      cy.wait(1000);
      cy.url().should('eq', `http://localhost:4200/startedHunts/${accessCode}`);
    });
    page.clickSecondBeginHuntButton();
  })

  it('should start hunt with the correct hunt information/end hunt page', () => {
    page.beginHuntButton().should('exist');
    page.beginHuntButton().click();
    cy.wait(2000);
    page.selectTeamNumber(5);
    page.clickProceedButton();
    cy.wait(1000);
    page.getAccessCode().then((accessCode) => {
      cy.wait(1000);
      cy.url().should('eq', `http://localhost:4200/startedHunts/${accessCode}`);
    });
    page.clickSecondBeginHuntButton();

    page.getHuntTaskList().should('exist');
    page.getTableTaskTitle().should('exist');
    page.getProgressTeamTile().should('exist');
    page.getTeamCard().should('exist');
  })

  it('should click End Hunt, navigate to the ended hunt details page', () => {
    page.beginHuntButton().should('exist');
    page.beginHuntButton().click();
    cy.wait(2000);
    page.selectTeamNumber(5);
    page.clickProceedButton();
    cy.wait(1000);
    page.getAccessCode().then((accessCode) => {
      page.getStartedHuntId(accessCode).then((id) => {
        const startedHuntId = id;
        cy.wait(1000);
        cy.url().should('eq', `http://localhost:4200/startedHunts/${accessCode}`);
        page.clickSecondBeginHuntButton();

        // start the hunt before end it

        page.clickEndHuntButton();
        cy.url().should('eq', `http://localhost:4200/endedHunts/${startedHuntId}`);
        cy.on('window:confirm', () => true);
      });
    });
  });

})
