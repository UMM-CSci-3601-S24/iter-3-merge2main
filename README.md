<img width="2061" alt="scavasnap_title" src="https://github.com/UMM-CSci-3601-S24/iter-3-merge2main/assets/143017587/3f517e82-c4a9-4a0c-8368-10e166587b78">


## Table of Contents

- [Table of Contents](#table-of-contents)
- [What is Scav-a-Snap](#what-is-scav-a-snap)
  - [How to Play Scav-a-Snap](#how-to-play-scav-a-snap)
- [Using Scav-a-Snap](#using-scav-a-snap)
  - [As a host](#as-a-host)
  - [As a hunter](#as-a-hunter)
- [Documentation](#documentation)
- [Testing and Continuous Integration](#testing-and-continuous-integration)
- [Authors](#authors)
- [Built With](#built-with)

## What is Scav-a-Snap

Scav-a-Snap is a scavenger hunt game for hosts and hunters to design and play their own created scavenger hunt. Scav-a-Snap is designed to be a child-friendly, creative-oriented game.

In a hunt of Scav-a-Snap there are two types of players - hosts and hunters. The host is responsible for creating a scavenger hunt and adding individual tasks to the hunt (for example "take a picture of a Stop Sign).

The game requires at least two people - a host and a hunter. However, ideally each hunt should have a few different hunters competing.

### How to Play Scav-a-Snap

A game of Scav-a-Snap consists of the host and the hunters.

The host is the one creates the hunt, adds tasks to the hunt for the hunters to do, and is responsible for moderating photos that the hunters submit.

The hunters are responsible for doing the tasks (which requires uploading photos for each task) in the least amount of time possible. The goal is to finish the hunt before the other hunters.

Each Scav-a-Snap hunt can contain an indefinite number of tasks. Each time the hunters upload a photo for each task, a green check mark will appear by said task. Once all the tasks are completed, the hunters will return back to where the host is. There, the host can go through all the photos that every hunter submitted. The host also as the option to delete photos that have been submitted if they would like to.

## Using Scav-a-Snap

### As a host

The first thing a host will want to do is click the "host" icon on the home page. This will bring them to the host page where they can create a new hunt, give it a name, and begin adding tasks to it.

Once they have added all the tasks they want to add, they can click "begin hunt". This will show an access code which the host will have to give to the hunters for them to join the created hunt.

### As a hunter

The first thing a hunter will want to do is click the "hunter" icon on the home page. This will allow them to type in an access code which will take them to a hunt. They will have to get this access code from the host. Once they type in the given access code, they will be brought to the started hunt with a list of tasks to do.  

## Documentation

- [Deployment Instructions](DEPLOYMENT.md)
- [Development Instructions](DEVELOPMENT.md)

## Testing and Continuous Integration

Scav-a-Snap uses Jasmine and Karma for unit testing through the Angular CLI. TravisCI is configured to run these tests.

## Authors

Scav-a-Snap was built over a semester by a team of students at the University of Minnesota Morris for a software design class.. See the GitHub (<https://github.com/orgs/UMM-CSci-3601-S24/teams/iteration3-merge2main>).

## Built With

Scav-a-Snap is powered by:

- [Angular](https://angular.io/)
- [Angular Material](https://material.angular.io/)
- [MongoDB](https://www.mongodb.com/)
- [Javalin](https://javalin.io/)
- [Digital Ocean](https://www.digitalocean.com/)

Other libraries used:

- [ng2-page-slider](https://github.com/KeatonTech/Angular-2-Page-Slider)
- [ng2-rx-componentdestroyed](https://github.com/w11k/ng2-rx-componentdestroyed)
- [ngx-qrcode2](https://github.com/techiediaries/ngx-qrcode)
- [ngx-pipes](https://github.com/danrevah/ngx-pipes)
- [ngx-clipboard](https://github.com/maxisam/ngx-clipboard)
- [time-ago-pipe](https://github.com/AndrewPoyntz/time-ago-pipe)

Tools:

- [Angular CLI](https://cli.angular.io/)
- [Node Package Manager](https://www.npmjs.com/)
- [TypeScript](https://www.typescriptlang.org/)
- [Karma](https://karma-runner.github.io/1.0/index.html)
- [Jasmine](https://jasmine.github.io/)
