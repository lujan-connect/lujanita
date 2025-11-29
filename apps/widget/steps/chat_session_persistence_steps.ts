import { Given, When, Then } from '@cucumber/cucumber';

Given('`sessionStorage` está disponible y el widget usa la clave {string}', function (key: string) {
  // TODO: Preparar sessionStorage
  this.pending = true;
});

Given('el usuario ha enviado {int} mensajes', function (count: number) {
  // TODO: Persistir mensajes
  this.pending = true;
});

Given('se guardan en sessionStorage', function () {
  // TODO
  this.pending = true;
});

When('el usuario recarga la página', function () {
  // TODO: Simular reload
  this.pending = true;
});

Then('el widget restaura la conversación desde sessionStorage', function () {
  // TODO: Verificar restauración
  this.pending = true;
});

