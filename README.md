# Odoo-to-draw.io-Converter
A program which converts Odoo's class entities / relationships to a Class diagram visualized in draw.io

# Example: the picture below shows Odoo's MRP Module Class Diagram
![MRP Module](https://github.com/mssm199996/Odoo-to-draw.io-Converter/blob/master/MRP%20Diagram.png)

# How to ?
1- The files models.xlsx and fields.xslx in this directory holds all data needed to create the draw.io diagram, and can be obtained by:
  - Acitvating Odoo's developper mode (menu -> settings -> general settings -> activate developper mode at the bottom of the page)
  - At the top, using the menu, going throught: Technical -> Database Structure -> Models (or fields) -> clicking the export all button (to the right of the import button)

2- The program loads these files (you can just put them in the project directory of change the program to point to these files by modifyin the MainPackage.MainClass.main() method

3- The first instruction in the main() method filters the result (to make sure you don't get a none readable diagram).
  - Example: Predicate<Module> modulePredicate = module -> module.getModuleId().startsWith("mrp");
  - This filter makes sure you get the Odoo's MRP Module class diagram (as shown in the picture above)

4- Once executed, the program generates a drawio.xml file (by default in the project's directory). To use it:
  - Go to draw.io (web or desktop version)
  - Using the top menu, Extras -> Edit Diagram... and you past there, the output of the drawio.xml file.
