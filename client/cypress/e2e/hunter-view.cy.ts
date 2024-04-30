import { HunterViewPage } from "cypress/support/hunter-view.po";
/// <reference types="cypress-file-upload" />

const page = new HunterViewPage();

describe('Hunter View', () => {
  beforeEach(() => page.navigateTo());

  /**
   * These test will get to the hunt, begin it and capture the access code.
   * Then it will navigate to the hunter view page with the access code.
   */

  it('should navigate to the right hunter view page with the captured access code', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());/// <reference types="cypress-file-upload" />
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    cy.wait(500);
    page.selectTeamNumber(1);
    page.clickHunterProceedButton();
    cy.wait(500);


    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunters/`);
      for (let i = 0; i < accessCode.length; i++) {
        page.getAccessCodeInput(i + 1).type(accessCode.toString().charAt(i));
      }
    }).then(() => {
      cy.wait(500);
      page.clickJoinHuntButton();
    });

    // navigate to the select team page.
    cy.wait(500);

    page.clickTeamButton();
    cy.wait(500);
    page.clickProceedButton();
    cy.wait(500);


    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunter-view/${accessCode}`);
    });
  });

  it('should display the hunt title as hunter-view', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    cy.wait(500);
    page.selectTeamNumber(1);
    page.clickHunterProceedButton();
    cy.wait(500);

    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunters/`);
      for (let i = 0; i < accessCode.length; i++) {
        page.getAccessCodeInput(i + 1).type(accessCode.toString().charAt(i));
      }
    }).then(() => {
      cy.wait(500);
      page.clickJoinHuntButton();
    })

    // navigate to the select team page.
    cy.wait(500);

    page.clickTeamButton();
    cy.wait(500);
    page.clickProceedButton();
    cy.wait(500);

    cy.url().should('match', /\/hunter-view\/\d+\/teams\/[0-9a-fA-F]{24}$/);

    page.getHunterViewTitle().contains('You are in');
  });

  it('should display the hunt tasks list in Your Task column', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    cy.wait(500);
    page.selectTeamNumber(1);
    page.clickHunterProceedButton();
    cy.wait(500);

    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunters/`);
      for (let i = 0; i < accessCode.length; i++) {
        page.getAccessCodeInput(i + 1).type(accessCode.toString().charAt(i));
      }
    }).then(() => {
      cy.wait(500);
      page.clickJoinHuntButton();
    })

    // navigate to the select team page.
    cy.wait(500);

    page.clickTeamButton();
    cy.wait(500);
    page.clickProceedButton();
    cy.wait(500);

    page.getHuntTaskList().should('exist');
  });

  it('should display the upload picture button and the picture input field', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    cy.wait(500);
    page.selectTeamNumber(1);
    page.clickHunterProceedButton();
    cy.wait(500);

    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunters/`);
      for (let i = 0; i < accessCode.length; i++) {
        page.getAccessCodeInput(i + 1).type(accessCode.toString().charAt(i));
      }
    }).then(() => {
      cy.wait(500);
      page.clickJoinHuntButton();
    })

    // navigate to the select team page.
    cy.wait(500);

    page.clickTeamButton();
    cy.wait(500);
    page.clickProceedButton();
    cy.wait(500);

    cy.url().should('match', /\/hunter-view\/\d+\/teams\/[0-9a-fA-F]{24}$/);

    page.getHunterUploadImage().should('exist');
  });

  it('should click the Upload Image button', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    cy.wait(1000);
    page.selectTeamNumber(1);
    page.clickHunterProceedButton();
    cy.wait(1000);

    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunters/`);
      cy.wait(2000); // wait for the page to load
      for (let i = 0; i < accessCode.length; i++) {
        page.getAccessCodeInput(i + 1).type(accessCode.toString().charAt(i));
      }
    }).then(() => {
      cy.wait(1000);
      page.clickJoinHuntButton();
    })

    // navigate to the select team page.
    cy.wait(500);

    page.clickTeamButton();
    cy.wait(500);
    page.clickProceedButton();
    cy.wait(500);

    cy.url().should('match', /\/hunter-view\/\d+\/teams\/[0-9a-fA-F]{24}$/);

    page.clickUploadImage();
  })


  it('should be able to delete the uploaded image and upload a new image in it\'s space', () => {
    page.getHostButton().click();
    page.getHuntCards().first().then(() => {
      page.clickViewProfile(page.getHuntCards().first());
      cy.url().should('match', /\/hunts\/[0-9a-fA-F]{24}$/);
    });

    page.clickBeginHunt();
    cy.wait(500);
    page.selectTeamNumber(1);
    page.clickHunterProceedButton();
    cy.wait(500);

    page.getAccessCode();

    // Those above will navigate to the Hunt, begin it
    // and capture the access code.

    cy.get('@accessCode').then((accessCode) => {
      cy.visit(`/hunters/`);
      for (let i = 0; i < accessCode.length; i++) {
        page.getAccessCodeInput(i + 1).type(accessCode.toString().charAt(i));
      }
    }).then(() => {
      cy.wait(500);
      page.clickJoinHuntButton();
    })

    // navigate to the select team page.
    cy.wait(500);

    page.clickTeamButton();
    cy.wait(500);
    page.clickProceedButton();
    cy.wait(500);

    // submits an image to the task

    cy.fixture('your-image.jpg').then(fileContent => {
      cy.get('input[type="file"]').attachFile({
        fileContent: fileContent.toString(),
        fileName: 'your-image.jpg',
        mimeType: 'image/jpg'
      });
    });

    // deletes the image

    page.clickDeleteImage();

    // checks that the delete button is not visible

    page.getDeleteImageButton().should('not.exist');

    // submits another image to the task

    cy.fixture('your-image.jpg').then(fileContent => {
      cy.get('input[type="file"]').attachFile({
        fileContent: fileContent.toString(),
        fileName: 'your-image.jpg',
        mimeType: 'image/jpg'
      });
    });

    // deletes the image

    page.clickDeleteImage();

    // checks that the delete button is not visible

    page.getDeleteImageButton().should('not.exist');

    //submits a third image to the task

    cy.fixture('your-image2.jpg').then(fileContent => {
      cy.get('input[type="file"]').attachFile({
        fileContent: fileContent.toString(),
        fileName: 'your-image.jpg',
        mimeType: 'image/jpg'
      });
    });

    // deletes the image

    page.clickDeleteImage();

    // checks that the delete button is not visible

    page.getDeleteImageButton().should('not.exist');
  })

});

